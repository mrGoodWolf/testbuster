package mchorse.blockbuster.client.gui.dashboard;

import mchorse.blockbuster.Blockbuster;
import mchorse.blockbuster.client.gui.framework.elements.GuiButtonElement;
import mchorse.blockbuster.client.gui.framework.elements.GuiElement;
import mchorse.blockbuster.client.gui.utils.Resizer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.config.GuiUtils;

/**
 * Dashboard sidebar GUI
 * 
 * Des-pa-ci-to.
 */
public class GuiDespacito extends GuiElement
{
    public GuiDashboard dashboard;

    public GuiDespacito(Minecraft mc, GuiDashboard dashboard)
    {
        super(mc);

        this.createChildren();
        this.dashboard = dashboard;

        GuiElement element = new GuiButtonElement<GuiSidebarButton>(mc, new GuiSidebarButton(0, 0, 0, new ItemStack(Blockbuster.registerItem)), (button) -> dashboard.openPanel(dashboard.mainPanel));
        Resizer resizer = new Resizer().set(4, 4, 24, 24).parent(this.area);
        this.children.add(element.setResizer(resizer));

        element = new GuiButtonElement<GuiSidebarButton>(mc, new GuiSidebarButton(0, 0, 0, new ItemStack(Blockbuster.directorBlock)), (button) -> dashboard.openPanel(dashboard.directorPanel));
        resizer = new Resizer().set(0, 24, 24, 24).relative(resizer);
        this.children.add(element.setResizer(resizer));

        element = new GuiButtonElement<GuiSidebarButton>(mc, new GuiSidebarButton(0, 0, 0, new ItemStack(Blockbuster.modelBlock)), (button) -> dashboard.openPanel(dashboard.modelPanel));
        resizer = new Resizer().set(0, 24, 24, 24).relative(resizer);
        this.children.add(element.setResizer(resizer));

        element = new GuiButtonElement<GuiSidebarButton>(mc, new GuiSidebarButton(0, 0, 0, new ItemStack(Blockbuster.actorConfigItem)), (button) -> dashboard.openPanel(dashboard.modelEditorPanel));
        resizer = new Resizer().set(0, 24, 24, 24).relative(resizer);
        this.children.add(element.setResizer(resizer));

        /* Despacito 13 confirmed */
        element = new GuiButtonElement<GuiSidebarButton>(mc, new GuiSidebarButton(0, 0, 0, new ItemStack(Items.RECORD_13)), (button) -> dashboard.openPanel(dashboard.recordingEditorPanel));
        resizer = new Resizer().set(0, 24, 24, 24).relative(resizer);
        this.children.add(element.setResizer(resizer));
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        int h = this.area.h;
        int x = this.area.x + this.area.w;

        this.mc.renderEngine.bindTexture(GuiDashboard.ICONS);
        GuiUtils.drawContinuousTexturedBox(this.area.x, this.area.y, 0, 32, this.area.w, h, 32, 32, 0, 0);
        mchorse.blockbuster.client.gui.utils.GuiUtils.drawHorizontalGradientRect(x, 0, x + 16, h, 0x22000000, 0x00000000, 0);
        mchorse.blockbuster.client.gui.utils.GuiUtils.drawHorizontalGradientRect(x - 8, 0, x, h, 0x00000000, 0x22000000, 0);
        Gui.drawRect(x - 1, this.area.y, x, this.area.getY(1), 0x22000000);

        super.draw(mouseX, mouseY, partialTicks);
    }
}