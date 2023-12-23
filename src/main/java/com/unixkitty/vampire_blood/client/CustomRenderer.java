package com.unixkitty.vampire_blood.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
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
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.renderable.BakedModelRenderable;
import net.minecraftforge.client.model.renderable.IRenderable;

import javax.annotation.Nonnull;

public class CustomRenderer
{
    public static final ResourceLocation HORNS = new ResourceLocation(VampireBlood.MODID, "custom/horns");
    public static final ResourceLocation TAIL_MAIN = new ResourceLocation(VampireBlood.MODID, "custom/tail_main");
    public static final ResourceLocation TAIL_SITTING = new ResourceLocation(VampireBlood.MODID, "custom/tail_sitting");
    public static final ResourceLocation TAIL_SPEED = new ResourceLocation(VampireBlood.MODID, "custom/tail_speed");

    public static class CosmeticLayer<T extends LivingEntity, M extends EntityModel<T>> extends
            RenderLayer<T, M>
    {
        private static IRenderable<ModelData> hornsRenderable;
        private static IRenderable<ModelData> tailMainRenderable;
        private static IRenderable<ModelData> tailSittingRenderable;
        private static IRenderable<ModelData> tailSpeedRenderable;

        public CosmeticLayer(RenderLayerParent<T, M> renderer)
        {
            super(renderer);
        }

        @Override
        public void render(@Nonnull PoseStack poseStack, @Nonnull MultiBufferSource renderTypeBuffer, int light, @Nonnull T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
        {
            if (
                    livingEntity instanceof Player player
                            && player.getStringUUID().equals("9d64fee0-582d-4775-b6ef-37d6e6d3f429")
                            && !player.isSpectator()
                            && player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).map(vampirePlayerData -> vampirePlayerData.getVampireLevel() == VampirismLevel.ORIGINAL).orElse(false)
            )
            {

                poseStack.pushPose();

                EntityRenderer<? super LivingEntity> render = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(livingEntity);

                if (hornsRenderable == null)
                {
                    hornsRenderable = BakedModelRenderable.of(HORNS).withModelDataContext();
                }

                if (tailMainRenderable == null)
                {
                    tailMainRenderable = BakedModelRenderable.of(TAIL_MAIN).withModelDataContext();
                }

                if (tailSittingRenderable == null)
                {
                    tailSittingRenderable = BakedModelRenderable.of(TAIL_SITTING).withModelDataContext();
                }

                if (tailSpeedRenderable == null)
                {
                    tailSpeedRenderable = BakedModelRenderable.of(TAIL_SPEED).withModelDataContext();
                }

                if (render instanceof LivingEntityRenderer)
                {
                    @SuppressWarnings("unchecked") LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> livingRenderer = (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) render;
                    EntityModel<LivingEntity> model = livingRenderer.getModel();

                    if (model instanceof HumanoidModel)
                    {
                        poseStack.pushPose();

                        ((HumanoidModel<LivingEntity>) model).head.translateAndRotate(poseStack);

                        poseStack.mulPose(Vector3f.XN.rotationDegrees(5F));
                        poseStack.translate(-0.25F, 0.05F, -0.25F);
                        poseStack.scale(0.5F, -0.5F, 0.5F);

                        hornsRenderable.render(poseStack, renderTypeBuffer, RenderType::entityTranslucent, light, OverlayTexture.NO_OVERLAY, partialTicks, ModelData.EMPTY);

                        poseStack.popPose();

                        poseStack.pushPose();

                        ((HumanoidModel<LivingEntity>) model).body.translateAndRotate(poseStack);

                        final IRenderable<ModelData> renderable;

                        if (player.isFallFlying() || player.isVisuallySwimming())
                        {
                            renderable = tailSpeedRenderable;

                            poseStack.mulPose(Vector3f.YP.rotationDegrees(-90F));
                            poseStack.mulPose(Vector3f.XP.rotationDegrees(180F));
                            poseStack.translate(0.05F, -2.45F, -0.75F);
                            poseStack.scale(2.0F, 2.0F, 1.5F);
                        }
                        else if (player.isPassenger())
                        {
                            renderable = tailSittingRenderable;

                            poseStack.mulPose(Vector3f.XP.rotationDegrees(90F));
                            poseStack.translate(-0.125F, -1.625F, -1.45F);
                            poseStack.scale(2.0F, 2.0F, 1.5F);
                        }
                        else if (player.hasPose(Pose.SLEEPING))
                        {
                            renderable = tailSittingRenderable;

                            poseStack.mulPose(Vector3f.XP.rotationDegrees(180F));
                            poseStack.translate(-0.05F, -2.45F, -0.85F);
                            poseStack.scale(2.0F, 2.0F, 1.5F);
                        }
                        else
                        {
                            renderable = tailMainRenderable;

                            poseStack.mulPose(Vector3f.YP.rotationDegrees(-90F));
                            poseStack.mulPose(Vector3f.XP.rotationDegrees(180F));
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
    }
}
