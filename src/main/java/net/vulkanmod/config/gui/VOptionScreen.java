package net.vulkanmod.config.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.vulkanmod.Initializer;
import net.vulkanmod.config.gui.widget.VAbstractWidget;
import net.vulkanmod.config.gui.widget.VButtonWidget;
import net.vulkanmod.config.option.OptionPage;
import net.vulkanmod.config.option.Options;
import net.vulkanmod.vulkan.util.ColorUtil;

import java.util.ArrayList;
import java.util.List;

public class VOptionScreen extends Screen {
    public final static int RED = ColorUtil.ARGB.pack(0.3f, 0.0f, 0.0f, 0.8f);
    final ResourceLocation ICON = ResourceLocation.fromNamespaceAndPath("vulkanmod", "vlogo_transparent.png");

    private final Screen parent;

    private final List<OptionPage> optionPages;

    private int currentListIdx = 0;

    private int tooltipX;
    private int tooltipY;
    private int tooltipWidth;

    private VButtonWidget supportButton;
    private VButtonWidget patcherButton;

    private VButtonWidget doneButton;
    private VButtonWidget applyButton;

    private final List<VButtonWidget> pageButtons = Lists.newArrayList();
    private final List<VButtonWidget> buttons = Lists.newArrayList();

    public VOptionScreen(Component title, Screen parent) {
        super(title);
        this.parent = parent;

        this.optionPages = new ArrayList<>();
    }

    private void addPages() {
        this.optionPages.clear();

        OptionPage page = new OptionPage(
                Component.translatable("vulkanmod.options.pages.video").getString(),
                Options.getVideoOpts()
        );
        this.optionPages.add(page);

        page = new OptionPage(
                Component.translatable("vulkanmod.options.pages.graphics").getString(),
                Options.getGraphicsOpts()
        );
        this.optionPages.add(page);

        page = new OptionPage(
                Component.translatable("vulkanmod.options.pages.optimizations").getString(),
                Options.getOptimizationOpts()
        );
        this.optionPages.add(page);

        page = new OptionPage(
                Component.translatable("vulkanmod.options.pages.other").getString(),
                Options.getOtherOpts()
        );
        this.optionPages.add(page);
    }

    @Override
    protected void init() {
        this.addPages();

        int top = 40;
        int bottom = 60;
        int itemHeight = 20;

        int leftMargin = 100;
//        int listWidth = (int) (this.width * 0.65f);
        int listWidth = Math.min((int) (this.width * 0.65f), 420);
        int listHeight = this.height - top - bottom;

        this.buildLists(leftMargin, top, listWidth, listHeight, itemHeight);

        int x = leftMargin + listWidth + 10;
//        int width = Math.min(this.width - this.tooltipX - 10, 200);
        int width = this.width - x - 10;
        int y = 50;

        if (width < 200) {
            x = 100;
            width = listWidth;
            y = this.height - bottom + 10;
        }

        this.tooltipX = x;
        this.tooltipY = y;
        this.tooltipWidth = width;

        buildPage();

        this.applyButton.active = false;
    }

    private void buildLists(int left, int top, int listWidth, int listHeight, int itemHeight) {
        for (OptionPage page : this.optionPages) {
            page.createList(left, top, listWidth, listHeight, itemHeight);
        }
    }

    private void addPageButtons(int x0, int y0, int width, int height, boolean verticalLayout) {
        int x = x0;
        int y = y0;
        for (int i = 0; i < this.optionPages.size(); ++i) {
            var page = this.optionPages.get(i);
            final int finalIdx = i;
            VButtonWidget widget = new VButtonWidget(x, y, width, height, Component.nullToEmpty(page.name), button -> this.setOptionList(finalIdx));
            this.buttons.add(widget);
            this.pageButtons.add(widget);
            this.addWidget(widget);

            if (verticalLayout)
                y += height + 1;
            else
                x += width + 1;
        }

        this.pageButtons.get(this.currentListIdx).setSelected(true);
    }

    private void buildPage() {
        this.buttons.clear();
        this.pageButtons.clear();
        this.clearWidgets();

//        this.addPageButtons(20, 6, 60, 20, false);
        this.addPageButtons(10, 40, 80, 22, true);

        VOptionList currentList = this.optionPages.get(this.currentListIdx).getOptionList();
        this.addWidget(currentList);

        this.addButtons();
    }

    private void addButtons() {
        int rightMargin = 20;
        int buttonHeight = 20;
        int padding = 10;
        int buttonMargin = 5;
        int buttonWidth = minecraft.font.width(CommonComponents.GUI_DONE) + 2 * padding;
        int x0 = (this.width - buttonWidth - rightMargin);
        int y0 = this.height - buttonHeight - 7;

        this.doneButton = new VButtonWidget(
                x0, y0,
                buttonWidth, buttonHeight,
                CommonComponents.GUI_DONE,
                button -> this.minecraft.setScreen(this.parent)
        );

        buttonWidth = minecraft.font.width(Component.translatable("\u0076\u0075\u006c\u006b\u0061\u006e\u006d\u006f\u0064\u002e\u006f\u0070\u0074\u0069\u006f\u006e\u0073\u002e\u0062\u0075\u0074\u0074\u006f\u006e\u0073\u002e\u0061\u0070\u0070\u006c\u0079")) + 2 * padding;
        x0 -= (buttonWidth + buttonMargin);
        this.applyButton = new VButtonWidget(
                x0, y0,
                buttonWidth, buttonHeight,
                Component.translatable("\u0076\u0075\u006c\u006b\u0061\u006e\u006d\u006f\u0064\u002e\u006f\u0070\u0074\u0069\u006f\u006e\u0073\u002e\u0062\u0075\u0074\u0074\u006f\u006e\u0073\u002e\u0061\u0070\u0070\u006c\u0079"),
                button -> this.applyOptions()
        );

        buttonWidth = minecraft.font.width(Component.translatable("\u0076\u0075\u006c\u006b\u0061\u006e\u006d\u006f\u0064\u002e\u006f\u0070\u0074\u0069\u006f\u006e\u0073\u002e\u0062\u0075\u0074\u0074\u006f\u006e\u0073\u002e\u006b\u006f\u0066\u0069")) + 10;
        x0 = (this.width - buttonWidth - rightMargin);
        this.supportButton = new VButtonWidget(
                x0, 6,
                buttonWidth, buttonHeight,
                Component.translatable("\u0076\u0075\u006c\u006b\u0061\u006e\u006d\u006f\u0064\u002e\u006f\u0070\u0074\u0069\u006f\u006e\u0073\u002e\u0062\u0075\u0074\u0074\u006f\u006e\u0073\u002e\u006b\u006f\u0066\u0069"),
                button -> Util.getPlatform().openUri("\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u006b\u006f\u002d\u0066\u0069\u002e\u0063\u006f\u006d\u002f\u0078\u0063\u006f\u006c\u006c\u0061\u0074\u0065\u0072\u0061\u006c")
        );

        buttonWidth = minecraft.font.width("\u0050\u0061\u0074\u0063\u0068\u0065\u0064\u0020\u0062\u0079\u0020\u00a7\u0065\u0053\u0068\u0061\u0064\u006f\u0077\u004d\u0043\u0036\u0039\u00a7\u0072") + 10;
        x0 = (x0 - buttonWidth - 6);
        this.patcherButton = new VButtonWidget(
                x0, 6,
                buttonWidth, buttonHeight,
                Component.literal("\u0050\u0061\u0074\u0063\u0068\u0065\u0064\u0020\u0062\u0079\u0020\u00a7\u0065\u0053\u0068\u0061\u0064\u006f\u0077\u004d\u0043\u0036\u0039\u00a7\u0072"),
                button -> Util.getPlatform().openUri("\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0079\u006f\u0075\u0074\u0075\u0062\u0065\u002e\u0063\u006f\u006d\u002f\u0063\u0068\u0061\u006e\u006e\u0065\u006c\u002f\u0055\u0043\u0064\u006f\u004e\u0031\u006b\u0072\u0067\u006e\u0079\u0064\u0063\u0079\u007a\u0071\u0067\u0052\u0045\u006a\u0066\u0044\u007a\u0051")
        );

        this.buttons.add(this.applyButton);
        this.buttons.add(this.doneButton);
        this.buttons.add(this.patcherButton);
        this.buttons.add(this.supportButton);

        this.addWidget(this.applyButton);
        this.addWidget(this.doneButton);
        this.addWidget(this.patcherButton);
        this.addWidget(this.supportButton);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (GuiEventListener element : this.children()) {
            if (element.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(element);
                if (button == 0) {
                    this.setDragging(true);
                }

                this.updateState();
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.setDragging(false);
        this.updateState();
        return this.getChildAt(mouseX, mouseY)
                .filter(guiEventListener -> guiEventListener.mouseReleased(mouseX, mouseY, button))
                .isPresent();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        if (this.minecraft.level == null) {
            this.renderPanorama(guiGraphics, f);
        }

        this.renderBlurredBackground(f);
        this.renderMenuBackground(guiGraphics);

    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics, 0, 0, delta);

        GuiRenderer.guiGraphics = guiGraphics;
        GuiRenderer.setPoseStack(guiGraphics.pose());

        RenderSystem.enableBlend();

        int size = minecraft.font.lineHeight * 4;

        guiGraphics.blit(ICON, 30, 4, 0f, 0f, size, size, size, size);

        VOptionList currentList = this.optionPages.get(this.currentListIdx).getOptionList();
        currentList.updateState(mouseX, mouseY);
        currentList.renderWidget(mouseX, mouseY);
        renderButtons(mouseX, mouseY);

        List<FormattedCharSequence> list = getHoveredButtonTooltip(currentList, mouseX, mouseY);
        if (list != null) {
            this.renderTooltip(list, this.tooltipX, this.tooltipY);
        }
    }

    public void renderButtons(int mouseX, int mouseY) {
        for (VButtonWidget button : buttons) {
            button.render(mouseX, mouseY);
        }
    }

    private void renderTooltip(List<FormattedCharSequence> list, int x, int y) {
        int padding = 3;
        int width = GuiRenderer.getMaxTextWidth(this.font, list);
        int height = list.size() * 10;
        float intensity = 0.05f;
        int color = ColorUtil.ARGB.pack(intensity, intensity, intensity, 0.6f);
        GuiRenderer.fill(x - padding, y - padding, x + width + padding, y + height + padding, color);

//        intensity = 0.4f;
//        color = ColorUtil.ARGB.pack(intensity, intensity, intensity, 0.9f);
        color = RED;
        GuiRenderer.renderBorder(x - padding, y - padding, x + width + padding, y + height + padding, 1, color);

        int yOffset = 0;
        for (var text : list) {
            GuiRenderer.drawString(this.font, text, x, y + yOffset, 0xffffffff);
            yOffset += 10;
        }
    }

    private List<FormattedCharSequence> getHoveredButtonTooltip(VOptionList buttonList, int mouseX, int mouseY) {
        VAbstractWidget widget = buttonList.getHoveredWidget(mouseX, mouseY);
        if (widget != null) {
            var tooltip = widget.getTooltip();
            if (tooltip == null)
                return null;

            return this.font.split(tooltip, this.tooltipWidth);
        }
        return null;
    }

    private void updateState() {
        boolean modified = false;
        for (var page : this.optionPages) {
            modified |= page.optionChanged();
        }

        this.applyButton.active = modified;
    }

    private void setOptionList(int i) {
        this.currentListIdx = i;

        this.buildPage();

        this.pageButtons.get(i).setSelected(true);
    }

    private void applyOptions() {
        List<OptionPage> pages = List.copyOf(this.optionPages);
        for (var page : pages) {
            page.applyOptionChanges();
        }

        Initializer.CONFIG.write();
    }
}
