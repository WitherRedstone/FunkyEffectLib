package com.chinaex123.funky_effect_lib.data;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEntityTypeTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class FELEntityTagsProvider extends EntityTypeTagsProvider {

    public FELEntityTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> pProvider, ExistingFileHelper fileHelper) {
        super(packOutput, pProvider, FunkyEffectLib.MOD_ID, fileHelper);
    }

    @Override
    public void addTags(HolderLookup.@NotNull Provider pProvider) {
        tag(FELEntityTypeTags.PERVADING_DARKNESS_MOB)
                .add(EntityType.ZOMBIE);
    }
}
