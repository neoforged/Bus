/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.bus;

import static java.lang.classfile.ClassFile.ACC_FINAL;
import static java.lang.classfile.ClassFile.ACC_PRIVATE;
import static java.lang.classfile.ClassFile.ACC_PUBLIC;
import static java.lang.constant.ConstantDescs.BSM_CLASS_DATA;
import static java.lang.constant.ConstantDescs.CD_MethodHandle;
import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.DEFAULT_NAME;
import static java.lang.constant.ConstantDescs.MTD_void;

import java.lang.classfile.ClassFile;
import java.lang.classfile.attribute.SourceFileAttribute;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventListener;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Manages generation of {@link EventListener} instances from a {@link SubscribeEvent} method,
 * by generating wrapper classes using ASM and loading them with {@code defineHiddenClass}.
 * This mechanism is the same as that used by lambdas.
 */
class EventListenerFactory {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private static final ClassDesc CD_GeneratedEventListener = ClassDesc.of(GeneratedEventListener.class.getName());

    private static final MethodType STATIC_HANDLER = MethodType.methodType(void.class, Event.class);
    private static final MethodType INSTANCE_HANDLER = MethodType.methodType(void.class, Object.class, Event.class);

    private static final MethodTypeDesc MTD_STATIC_HANDLER = STATIC_HANDLER.describeConstable().orElseThrow();
    private static final MethodTypeDesc MTD_INSTANCE_HANDLER = INSTANCE_HANDLER.describeConstable().orElseThrow();

    private static final MethodType STATIC_CONSTRUCTOR = MethodType.methodType(void.class);
    private static final MethodType INSTANCE_CONSTRUCTOR = MethodType.methodType(void.class, Object.class);

    private static final LockHelper<Method, MethodHandle> eventListenerFactories = LockHelper.withHashMap();

    private static MethodHandle getEventListenerFactory(Method m) {
        return eventListenerFactories.computeIfAbsent(m, EventListenerFactory::createWrapper0);
    }

    private static MethodHandle createWrapper0(Method callback) {
        try {
            callback.setAccessible(true);

            var handle = LOOKUP.unreflect(callback);
            var isStatic = Modifier.isStatic(callback.getModifiers());
            var ctorType = isStatic ? STATIC_CONSTRUCTOR : INSTANCE_CONSTRUCTOR;

            var boxedHandle = handle.asType(isStatic ? STATIC_HANDLER : INSTANCE_HANDLER);

            var classBytes = makeClass(EventListenerFactory.class.getName() + "$" + callback.getName(), isStatic, ctorType);
            var classLookup = LOOKUP.defineHiddenClassWithClassData(classBytes, boxedHandle, true);
            return classLookup.findConstructor(classLookup.lookupClass(), ctorType);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create listener", e);
        }
    }

    protected static byte[] makeClass(String name, boolean isStatic, MethodType ctorType) {
        var classDesc = ClassDesc.of(name);
        return ClassFile.of().build(classDesc, clb -> {
            clb.withFlags(AccessFlag.PUBLIC, AccessFlag.FINAL)
                    .withSuperclass(CD_GeneratedEventListener)
                    .with(SourceFileAttribute.of(".dynamic"));

            if (!isStatic) {
                clb.withField("instance", CD_Object, ACC_PRIVATE | ACC_FINAL);
            }
            clb.withMethodBody("<init>", ctorType.describeConstable().orElseThrow(), ACC_PUBLIC, cob -> {
                cob.aload(0);
                cob.invokespecial(CD_GeneratedEventListener, "<init>", MTD_void);
                if (!isStatic) {
                    cob.aload(0);
                    cob.aload(1);
                    cob.putfield(ClassDesc.of(name), "instance", CD_Object);
                }
                cob.return_();
            });
            clb.withMethodBody("invoke", MTD_STATIC_HANDLER, ACC_PUBLIC, cob -> {
                var cp = cob.constantPool();
                cob.ldc(
                        cp.constantDynamicEntry(
                                cp.bsmEntry(BSM_CLASS_DATA, List.of()),
                                cp.nameAndTypeEntry(DEFAULT_NAME, CD_MethodHandle)));
                if (!isStatic) {
                    cob.aload(0);
                    cob.getfield(ClassDesc.of(name), "instance", CD_Object);
                }
                cob.aload(1);
                cob.invokevirtual(CD_MethodHandle, "invokeExact", isStatic ? MTD_STATIC_HANDLER : MTD_INSTANCE_HANDLER);
                cob.return_();
            });
        });
    }

    public static EventListener create(Method callback, Object target) {
        try {
            var factory = getEventListenerFactory(callback);

            if (Modifier.isStatic(callback.getModifiers())) {
                return (EventListener) factory.invoke();
            } else {
                return (EventListener) factory.invoke(target);
            }
        } catch (Throwable e) {
            throw new RuntimeException("Failed to create IEventListener", e);
        }
    }
}
