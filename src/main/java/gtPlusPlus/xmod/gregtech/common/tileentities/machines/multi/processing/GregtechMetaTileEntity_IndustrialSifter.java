package gtPlusPlus.xmod.gregtech.common.tileentities.machines.multi.processing;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.onElementPass;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static gregtech.api.enums.GT_HatchElement.*;
import static gregtech.api.util.GT_StructureUtility.buildHatchAdder;

import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import gregtech.api.enums.TAE;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.GT_Multiblock_Tooltip_Builder;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import gtPlusPlus.core.block.ModBlocks;
import gtPlusPlus.core.lib.CORE;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.base.GregtechMeta_MultiBlockBase;
import gtPlusPlus.xmod.gregtech.common.blocks.textures.TexturesGtBlock;
import java.util.Random;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public class GregtechMetaTileEntity_IndustrialSifter
        extends GregtechMeta_MultiBlockBase<GregtechMetaTileEntity_IndustrialSifter> implements ISurvivalConstructable {

    private int mCasing;
    private static IStructureDefinition<GregtechMetaTileEntity_IndustrialSifter> STRUCTURE_DEFINITION = null;

    public GregtechMetaTileEntity_IndustrialSifter(final int aID, final String aName, final String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GregtechMetaTileEntity_IndustrialSifter(final String aName) {
        super(aName);
    }

    @Override
    public IMetaTileEntity newMetaEntity(final IGregTechTileEntity aTileEntity) {
        return new GregtechMetaTileEntity_IndustrialSifter(this.mName);
    }

    @Override
    public String getMachineType() {
        return "Sifter";
    }

    @Override
    protected GT_Multiblock_Tooltip_Builder createTooltip() {
        GT_Multiblock_Tooltip_Builder tt = new GT_Multiblock_Tooltip_Builder();
        tt.addMachineType(getMachineType())
                .addInfo("Controller Block for the Industrial Sifter")
                .addInfo("400% faster than single-block machines of the same voltage")
                .addInfo("Only uses 75% of the EU/t normally required")
                .addInfo("Processes four items per voltage tier")
                .addPollutionAmount(getPollutionPerSecond(null))
                .addSeparator()
                .beginStructureBlock(5, 3, 5, false)
                .addController("Bottom Center")
                .addCasingInfo("Sieve Grate", 18)
                .addCasingInfo("Sieve Casings", 35)
                .addInputBus("Any Casing", 1)
                .addOutputBus("Any Casing (x4)", 1)
                .addInputHatch("Any Casing", 1)
                .addOutputHatch("Any Casing", 1)
                .addEnergyHatch("Any Casing", 1)
                .addMaintenanceHatch("Any Casing", 1)
                .addMufflerHatch("Any Casing", 1)
                .toolTipFinisher(CORE.GT_Tooltip_Builder);
        return tt;
    }

    @Override
    public IStructureDefinition<GregtechMetaTileEntity_IndustrialSifter> getStructureDefinition() {
        if (STRUCTURE_DEFINITION == null) {
            STRUCTURE_DEFINITION = StructureDefinition.<GregtechMetaTileEntity_IndustrialSifter>builder()
                    .addShape(mName, transpose(new String[][] {
                        {"CCCCC", "CMMMC", "CMMMC", "CMMMC", "CCCCC"},
                        {"CCCCC", "CMMMC", "CMMMC", "CMMMC", "CCCCC"},
                        {"CC~CC", "CCCCC", "CCCCC", "CCCCC", "CCCCC"},
                    }))
                    .addElement(
                            'C',
                            buildHatchAdder(GregtechMetaTileEntity_IndustrialSifter.class)
                                    .atLeast(InputBus, OutputBus, Maintenance, Energy, Muffler, InputHatch, OutputHatch)
                                    .casingIndex(TAE.GTPP_INDEX(21))
                                    .dot(1)
                                    .buildAndChain(
                                            onElementPass(x -> ++x.mCasing, ofBlock(ModBlocks.blockCasings2Misc, 5))))
                    .addElement('M', ofBlock(ModBlocks.blockCasings2Misc, 6))
                    .build();
        }
        return STRUCTURE_DEFINITION;
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece(mName, stackSize, hintsOnly, 2, 2, 0);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) return -1;
        return survivialBuildPiece(mName, stackSize, 2, 2, 0, elementBudget, env, false, true);
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        mCasing = 0;
        return checkPiece(mName, 2, 2, 0) && mCasing >= 35 && mOutputBusses.size() >= 4 && checkHatch();
    }

    @Override
    protected IIconContainer getActiveOverlay() {
        return TexturesGtBlock.Overlay_Machine_Controller_Default_Active;
    }

    @Override
    protected IIconContainer getInactiveOverlay() {
        return TexturesGtBlock.Overlay_Machine_Controller_Default;
    }

    @Override
    protected int getCasingTextureId() {
        return TAE.GTPP_INDEX(21);
    }

    @Override
    public GT_Recipe.GT_Recipe_Map getRecipeMap() {
        return GT_Recipe.GT_Recipe_Map.sSifterRecipes;
    }

    @Override
    public void onPreTick(final IGregTechTileEntity aBaseMetaTileEntity, final long aTick) {
        super.onPreTick(aBaseMetaTileEntity, aTick);
        if ((aBaseMetaTileEntity.isClientSide())
                && (aBaseMetaTileEntity.isActive())
                && (aBaseMetaTileEntity.getFrontFacing() != 1)
                && (aBaseMetaTileEntity.getCoverIDAtSide((byte) 1) == 0)
                && (!aBaseMetaTileEntity.getOpacityAtSide((byte) 1))) {
            final Random tRandom = aBaseMetaTileEntity.getWorld().rand;
            if (tRandom.nextFloat() > 0.4) return;

            final int xDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetX * 2;
            final int zDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetZ * 2;

            aBaseMetaTileEntity
                    .getWorld()
                    .spawnParticle(
                            "smoke",
                            (aBaseMetaTileEntity.getXCoord() + xDir + 2.1F) - (tRandom.nextFloat() * 3.2F),
                            aBaseMetaTileEntity.getYCoord() + 2.5f + (tRandom.nextFloat() * 1.2F),
                            (aBaseMetaTileEntity.getZCoord() + zDir + 2.1F) - (tRandom.nextFloat() * 3.2F),
                            0.0,
                            0.0,
                            0.0);
        }
    }

    @Override
    public boolean checkRecipe(final ItemStack aStack) {
        return checkRecipeGeneric((4 * GT_Utility.getTier(this.getMaxInputVoltage())), 75, 400, 10000);
    }

    @Override
    protected boolean doesMachineBoostOutput() {
        return false;
    }

    @Override
    public int getMaxParallelRecipes() {
        return (4 * GT_Utility.getTier(this.getMaxInputVoltage()));
    }

    @Override
    public int getEuDiscountForParallelism() {
        return 75;
    }

    @Override
    public int getMaxEfficiency(final ItemStack aStack) {
        return 10000;
    }

    @Override
    public int getPollutionPerSecond(final ItemStack aStack) {
        return CORE.ConfigSwitches.pollutionPerSecondMultiIndustrialSifter;
    }

    @Override
    public int getAmountOfOutputs() {
        return 16;
    }

    @Override
    public boolean explodesOnComponentBreak(final ItemStack aStack) {
        return false;
    }

    @Override
    public boolean isOverclockerUpgradable() {
        return true;
    }
}
