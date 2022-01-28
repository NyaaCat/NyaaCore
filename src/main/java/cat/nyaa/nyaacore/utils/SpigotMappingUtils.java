package cat.nyaa.nyaacore.utils;

import cat.nyaa.nyaacore.NyaaCoreLoader;
import cat.nyaa.nyaacore.Pair;
import org.cadixdev.bombe.type.FieldType;
import org.cadixdev.bombe.type.signature.FieldSignature;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.io.srg.csrg.CSrgReader;
import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.lorenz.model.FieldMapping;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

public class SpigotMappingUtils {
    static String FILE_NAME = "spigot-deobf.csrg";
    @Nullable
    static MappingSet deobfMappingSet;//DEOBF (obf->deobf)
    @Nullable
    static MappingSet obfMappingSet; //obf (deobf->obf)

    public static void init(NyaaCoreLoader nyaaCoreLoader) {
        InputStream inputStream = nyaaCoreLoader.getResource(FILE_NAME);
        if (inputStream == null) throw new RuntimeException("Resource " + FILE_NAME + " not found");
        try {
            deobfMappingSet = new CSrgReader(new InputStreamReader(inputStream)).read();
            obfMappingSet = deobfMappingSet.reverse();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Can not load " + FILE_NAME);
        }
    }

    public static MappingSet getObfMappingSet() {
        if (obfMappingSet == null) {
            throw new RuntimeException("obfMappingSet has not been loaded");
        }
        return obfMappingSet;
    }

    public static MappingSet getDeobfMappingSet() {
        if (deobfMappingSet == null) {
            throw new RuntimeException("deobfMappingSet has not been loaded");
        }
        return deobfMappingSet;
    }

    public static Optional<? extends ClassMapping<?, ?>> getObfuscatedClassMapping(String deobfuscatedClassName) {
        return getObfMappingSet().getClassMapping(deobfuscatedClassName);
    }

    public static Optional<String> getSimpleObfuscatedFieldNameOptional(String deobfuscatedClassName, String fieldName, @Nullable FieldType fieldType) {
        return getObfuscatedClassAndFieldMapping(deobfuscatedClassName, fieldName, fieldType).map(fieldMappingPair -> fieldMappingPair.getValue().getSimpleDeobfuscatedName());
    }

    private static Optional<Pair<? extends ClassMapping<?, ?>, FieldMapping>> getObfuscatedClassAndFieldMapping(String deobfuscatedClassName, String fieldName, @Nullable FieldType fieldType) {
        Optional<? extends ClassMapping<?, ?>> optionalClassMapping = getObfuscatedClassMapping(deobfuscatedClassName);
        if (optionalClassMapping.isEmpty()) return Optional.empty();
        Optional<FieldMapping> optionalFieldMapping = optionalClassMapping.get().computeFieldMapping(new FieldSignature(fieldName, fieldType));
        if (optionalFieldMapping.isEmpty()) return Optional.empty();
        return Optional.of(Pair.of(optionalClassMapping.get(), optionalFieldMapping.get()));
    }
}
