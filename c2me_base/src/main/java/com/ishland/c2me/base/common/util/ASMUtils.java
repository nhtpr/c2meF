package com.ishland.c2me.base.common.util;

import cpw.mods.modlauncher.api.INameMappingService;
import net.minecraftforge.fml.loading.FMLLoader;
import org.objectweb.asm.Type;

import java.util.Arrays;

public class ASMUtils {
    public static final String SRG = "srg";

    public static String remapMethodDescriptor(String desc) {
        final Type returnType = Type.getReturnType(desc);
        final Type[] argumentTypes = Type.getArgumentTypes(desc);
        return Type.getMethodDescriptor(
                Type.getType(remapFieldDescriptor(returnType.getDescriptor())),
                Arrays.stream(argumentTypes)
                        .map(type -> Type.getType(remapFieldDescriptor(type.getDescriptor())))
                        .toArray(Type[]::new)
        );
    }

    public static String remapFieldDescriptor(String desc) {
        final Type type = Type.getType(desc);
        if (type.getSort() == Type.ARRAY) {
            return "[".repeat(type.getDimensions()) + remapFieldDescriptor(type.getElementType().getDescriptor());
        }
        if (type.getSort() != Type.OBJECT) {
            return desc;
        }
        final String unmappedClassDesc = type.getClassName();
        final String unmappedClass;
        if (unmappedClassDesc.endsWith(";") && unmappedClassDesc.startsWith("L")) {
            unmappedClass = unmappedClassDesc.substring(1, unmappedClassDesc.length() - 1);
        } else {
            unmappedClass = unmappedClassDesc;
        }

        String mappedClass = unmappedClass.replace('/', '.');
        try {
            var nameFunction = FMLLoader.getNameFunction("srg").orElse(null);
            if (nameFunction != null) {
                mappedClass = nameFunction.apply(INameMappingService.Domain.CLASS, mappedClass);
            }
        } catch (Exception e) {

        }

        return 'L' + mappedClass.replace('.', '/') + ";";
    }
}