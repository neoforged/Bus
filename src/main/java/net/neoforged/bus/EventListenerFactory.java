/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.bus;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V16;

import java.lang.constant.ConstantDescs;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventListener;
import net.neoforged.bus.api.SubscribeEvent;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

/**
 * Manages generation of {@link EventListener} instances from a {@link SubscribeEvent} method,
 * by generating wrapper classes using ASM and loading them with {@code defineHiddenClass}.
 * This mechanism is the same as that used by lambdas.
 */
class EventListenerFactory {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private static final String HANDLER_DESC = Type.getInternalName(GeneratedEventListener.class);

    private static final String HANDLER_FUNC_DESC = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Event.class));
    private static final String INSTANCE_FUNC_DESC = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class), Type.getType(Event.class));

    private static final MethodType STATIC_HANDLER = MethodType.methodType(void.class, Event.class);
    private static final MethodType INSTANCE_HANDLER = MethodType.methodType(void.class, Object.class, Event.class);

    private static final MethodType STATIC_CONSTRUCTOR = MethodType.methodType(void.class);
    private static final MethodType INSTANCE_CONSTRUCTOR = MethodType.methodType(void.class, Object.class);

    private static final ConstantDynamic METHOD_CONSTANT = new ConstantDynamic(ConstantDescs.DEFAULT_NAME, MethodHandle.class.descriptorString(), new Handle(H_INVOKESTATIC, Type.getInternalName(MethodHandles.class), "classData", MethodType.methodType(Object.class, MethodHandles.Lookup.class, String.class, Class.class).descriptorString(), false));

    private static final LockHelper<Method, MethodHandle> eventListenerFactories = LockHelper.withHashMap();

    private static MethodHandle getEventListenerFactory(Method m) {
        return eventListenerFactories.computeIfAbsent(m, EventListenerFactory::createWrapper0);
    }

    private static MethodHandle createWrapper0(Method callback) {
        try {
            callback.setAccessible(true);

            var handle = LOOKUP.unreflect(callback);
            var isStatic = Modifier.isStatic(callback.getModifiers());

            var boxedHandle = handle.asType(isStatic ? STATIC_HANDLER : INSTANCE_HANDLER);

            var classBytes = makeClass(EventListenerFactory.class.getName() + "$" + callback.getName(), isStatic);
            var classLookup = LOOKUP.defineHiddenClassWithClassData(classBytes, boxedHandle, true);
            return classLookup.findConstructor(classLookup.lookupClass(), isStatic ? STATIC_CONSTRUCTOR : INSTANCE_CONSTRUCTOR);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create listener", e);
        }
    }

    protected static byte[] makeClass(String name, boolean isStatic) {
        ClassWriter cv = new ClassWriter(0);

        String desc = name.replace('.', '/');
        cv.visit(V16, ACC_PUBLIC | ACC_FINAL, desc, null, HANDLER_DESC, null);

        cv.visitSource(".dynamic", null);
        if (!isStatic) {
            cv.visitField(ACC_PRIVATE | ACC_FINAL, "instance", "Ljava/lang/Object;", null, null).visitEnd();
        }
        {
            var mv = cv.visitMethod(ACC_PUBLIC, "<init>", isStatic ? "()V" : "(Ljava/lang/Object;)V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, HANDLER_DESC, "<init>", "()V", false);
            if (!isStatic) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitFieldInsn(PUTFIELD, desc, "instance", "Ljava/lang/Object;");
            }
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        {
            var mv = cv.visitMethod(ACC_PUBLIC, "invoke", HANDLER_FUNC_DESC, null, null);
            mv.visitCode();
            mv.visitLdcInsn(METHOD_CONSTANT);
            if (!isStatic) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, desc, "instance", "Ljava/lang/Object;");
            }
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "invokeExact", isStatic ? HANDLER_FUNC_DESC : INSTANCE_FUNC_DESC, false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(3, 2);
            mv.visitEnd();
        }
        cv.visitEnd();

        return cv.toByteArray();
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
