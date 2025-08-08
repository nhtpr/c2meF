package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.asm;

import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;
import net.minecraft.world.level.levelgen.structure.structures.MineshaftPieces;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressPieces;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentPieces;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdPieces;
import net.minecraft.world.level.levelgen.structure.structures.SwampHutPiece;
import net.minecraft.world.level.levelgen.structure.structures.WoodlandMansionPieces;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = {
        MineshaftPieces.MineShaftCorridor.class,
        NetherFortressPieces.MonsterThrone.class,
        NetherFortressPieces.CastleSmallCorridorLeftTurnPiece.class,
        NetherFortressPieces.CastleSmallCorridorRightTurnPiece.class,
        NetherFortressPieces.StartPiece.class,
        OceanMonumentPieces.RoomDefinition.class,
        OceanMonumentPieces.MonumentBuilding.class,
        PoolElementStructurePiece.class,
        StrongholdPieces.ChestCorridor.class,
        StrongholdPieces.PortalRoom.class,
        StrongholdPieces.StartPiece.class,
        ScatteredFeaturePiece.class,
        SwampHutPiece.class,
        WoodlandMansionPieces.PlacementData.class,
})
public class ASMTargets {
}
