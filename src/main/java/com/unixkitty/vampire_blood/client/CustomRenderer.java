package com.unixkitty.vampire_blood.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.player.VampirismLevel;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.renderable.BakedModelRenderable;
import net.minecraftforge.client.model.renderable.IRenderable;

import javax.annotation.Nonnull;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class CustomRenderer
{
    public static final ResourceLocation HORNS = new ResourceLocation(VampireBlood.MODID, "custom/horns");
    public static final ResourceLocation TAIL_MAIN = new ResourceLocation(VampireBlood.MODID, "custom/tail_main");
    public static final ResourceLocation TAIL_SITTING = new ResourceLocation(VampireBlood.MODID, "custom/tail_sitting");
    public static final ResourceLocation TAIL_SPEED = new ResourceLocation(VampireBlood.MODID, "custom/tail_speed");
    public static final ResourceLocation WINGS = new ResourceLocation(VampireBlood.MODID, "custom/wing");

    public static class CosmeticLayer<T extends LivingEntity, M extends EntityModel<T>> extends
            RenderLayer<T, M>
    {
        private final IRenderable<ModelData> hornsRenderable;
        private final IRenderable<ModelData> tailMainRenderable;
        private final IRenderable<ModelData> tailSittingRenderable;
        private final IRenderable<ModelData> tailSpeedRenderable;
        private final IRenderable<ModelData> wingsRenderable;
        private final UUID originalID = UUID.fromString("9d64fee0-582d-4775-b6ef-37d6e6d3f429");

        public CosmeticLayer(RenderLayerParent<T, M> renderer)
        {
            super(renderer);

            this.hornsRenderable = BakedModelRenderable.of(HORNS).withModelDataContext();
            this.tailMainRenderable = BakedModelRenderable.of(TAIL_MAIN).withModelDataContext();
            this.tailSittingRenderable = BakedModelRenderable.of(TAIL_SITTING).withModelDataContext();
            this.tailSpeedRenderable = BakedModelRenderable.of(TAIL_SPEED).withModelDataContext();
            this.wingsRenderable = BakedModelRenderable.of(WINGS).withModelDataContext();
        }

        @Override
        public void render(@Nonnull PoseStack poseStack, @Nonnull MultiBufferSource renderTypeBuffer, int light, @Nonnull T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
        {
            if (
                    livingEntity instanceof Player player
                            && player.getUUID().equals(this.originalID)
                            && !player.isSpectator()
                            && !player.isInvisible()
                            && player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).map(vampirePlayerData -> vampirePlayerData.getVampireLevel() == VampirismLevel.ORIGINAL).orElse(player.isCreative())
            )
            {
                poseStack.pushPose();

                EntityRenderer<? super LivingEntity> render = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(livingEntity);

                if (render instanceof LivingEntityRenderer)
                {
                    @SuppressWarnings("unchecked") LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> livingRenderer = (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) render;
                    EntityModel<LivingEntity> model = livingRenderer.getModel();

                    if (model instanceof HumanoidModel)
                    {
                        //HORNS
                        poseStack.pushPose();

                        ((HumanoidModel<LivingEntity>) model).head.translateAndRotate(poseStack);

                        poseStack.mulPose(Axis.XN.rotationDegrees(5F));

                        poseStack.translate(-0.25F, 0.05F, -0.25F);
                        poseStack.scale(0.5F, -0.5F, 0.5F);

                        hornsRenderable.render(poseStack, renderTypeBuffer, RenderType::entityTranslucent, light, OverlayTexture.NO_OVERLAY, partialTicks, ModelData.EMPTY);

                        poseStack.popPose();

                        if (player.hasPose(Pose.FALL_FLYING))
                        {
                            //WING LEFT
                            renderWing(poseStack, model, renderTypeBuffer, light, partialTicks, 167.5F, -12.5F, -2.825F, -1.8F, -0.4F, 3F, 2.75F);

                            //WING RIGHT
                            renderWing(poseStack, model, renderTypeBuffer, light, partialTicks, 12.5F, -12.5F, -2.825F, -1.8F, -0.6F, 3F, 2.75F);
                        }
                        else
                        {
                            //WING LEFT
                            renderWing(poseStack, model, renderTypeBuffer, light, partialTicks, 147.5F, 0F, -2.075F, -1.375F, -0.4F, 2.0F, 2.0F); //157.5F

                            //WING RIGHT
                            renderWing(poseStack, model, renderTypeBuffer, light, partialTicks, 32.5F, 0F, -2.075F, -1.375F, -0.6F, 2.0F, 2.0F); //22.5F
                        }

                        //TAIL
                        poseStack.pushPose();

                        ((HumanoidModel<LivingEntity>) model).body.translateAndRotate(poseStack);

                        final IRenderable<ModelData> renderable;

                        if (player.isFallFlying() || player.isVisuallySwimming())
                        {
                            renderable = tailSpeedRenderable;

                            poseStack.mulPose(Axis.YP.rotationDegrees(-90F));
                            poseStack.mulPose(Axis.XP.rotationDegrees(180F));
                            poseStack.translate(0.05F, -2.45F, -0.75F);
                            poseStack.scale(2.0F, 2.0F, 1.5F);
                        }
                        else if (player.isPassenger())
                        {
                            renderable = tailSittingRenderable;

                            poseStack.mulPose(Axis.XP.rotationDegrees(90F));
                            poseStack.translate(-0.125F, -1.625F, -1.45F);
                            poseStack.scale(2.0F, 2.0F, 1.5F);
                        }
                        else if (player.hasPose(Pose.SLEEPING))
                        {
                            renderable = tailSittingRenderable;

                            poseStack.mulPose(Axis.XP.rotationDegrees(180F));
                            poseStack.translate(-0.05F, -2.45F, -0.85F);
                            poseStack.scale(2.0F, 2.0F, 1.5F);
                        }
                        else
                        {
                            renderable = tailMainRenderable;

                            poseStack.mulPose(Axis.YP.rotationDegrees(-90F));
                            poseStack.mulPose(Axis.XP.rotationDegrees(180F));
                            poseStack.translate(0.05F, -1.5F, -0.75F);
                            poseStack.scale(1.0F, 1.0F, 1.5F);
                        }

                        renderable.render(poseStack, renderTypeBuffer, RenderType::entityTranslucent, light, OverlayTexture.NO_OVERLAY, partialTicks, ModelData.EMPTY);

                        poseStack.popPose();
                    }
                }

                poseStack.popPose();
            }
        }

        private void renderWing(PoseStack poseStack, EntityModel<LivingEntity> model, MultiBufferSource renderTypeBuffer, int light, float partialTicks, float yRotation, float zRotation, float xTranslate, float yTranslate, float zTranslate, float xScale, float yScale)
        {
            poseStack.pushPose();

            ((HumanoidModel<LivingEntity>) model).body.translateAndRotate(poseStack);

            poseStack.mulPose(Axis.YP.rotationDegrees(yRotation));
            poseStack.mulPose(Axis.XP.rotationDegrees(180F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(zRotation));
            poseStack.translate(xTranslate, yTranslate, zTranslate);
            poseStack.scale(xScale, yScale, 1F);

            this.wingsRenderable.render(poseStack, renderTypeBuffer, RenderType::entityTranslucent, light, OverlayTexture.NO_OVERLAY, partialTicks, ModelData.EMPTY);

            poseStack.popPose();
        }
    }
}
