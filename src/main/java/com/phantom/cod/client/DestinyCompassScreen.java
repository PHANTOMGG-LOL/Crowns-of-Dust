package com.phantom.cod.client;

import com.phantom.cod.CrownsOfDust;
import com.phantom.cod.network.ModNetwork;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;
import java.util.Optional;

public class DestinyCompassScreen extends BookViewScreen {

    // ==================================================
    // CONSTRUCTOR
    // ==================================================

    public DestinyCompassScreen() {

        super(
                new BookAccess(
                        List.of(
                                createPageOne(),
                                createAshenreachPage(),
                                createEldoriaPage(),
                                createValdrenPage(),
                                createMyrathPage(),
                                createTemplePage(),
                                createCorePage(),
                                createCoreCraftingPage()
                        )
                )
        );
    }


    // ==================================================
    // PAGE 1 — DESTINY COMPASS
    // ==================================================

    private static Component createPageOne() {

        MutableComponent page =
                Component.empty();

        page.append(
                Component.literal("DESTINY COMPASS")
                        .withStyle(
                                ChatFormatting.BOLD
                        )
        );

        page.append("\n\n");

        page.append(
                Component.literal(
                        "An ancient compass\n"
                                + "bound to forgotten\n"
                                + "destinies."
                )
        );

        page.append("\n\n");

        page.append(
                Component.literal(
                        "Its needle knows\n"
                                + "the way."
                ).withStyle(
                        ChatFormatting.ITALIC
                )
        );

        page.append("\n\n");

        page.append(
                Component.literal(
                        "Turn the pages.\n"
                                + "Choose your path."
                ).withStyle(
                        ChatFormatting.BOLD
                )
        );

        return page;
    }


    // ==================================================
    // PAGE 2 — ASHENREACH
    // ==================================================

    private static Component createAshenreachPage() {

        MutableComponent page =
                Component.empty();

        page.append(
                Component.literal(
                        "KINGDOM OF ASHENREACH"
                ).withStyle(
                        ChatFormatting.BOLD
                )
        );

        page.append("\n\n");

        page.append(
                Component.literal(
                        "A kingdom of warriors\n"
                                + "and conquerors."
                )
        );

        page.append("\n\n");

        page.append(
                Component.literal(
                        "Built upon strength,\n"
                                + "wealth, and ambition."
                )
        );

        page.append("\n\n");

        page.append(
                Component.literal(
                        "Its ruins still remain."
                )
        );

        page.append("\n\n");

        page.append(
                destinationLink(
                        "Way to Ashenreach",
                        "ashenreach"
                )
        );

        return page;
    }


    // ==================================================
    // PAGE 3 — ELDORIA
    // ==================================================

    private static Component createEldoriaPage() {

        MutableComponent page =
                Component.empty();

        page.append(
                Component.literal(
                        "KINGDOM OF ELDORIA"
                ).withStyle(
                        ChatFormatting.BOLD
                )
        );

        page.append("\n\n");

        page.append(
                Component.literal(
                        "A kingdom of scholars\n"
                                + "and seekers of truth."
                )
        );

        page.append("\n\n");

        page.append(
                Component.literal(
                        "They recorded what\n"
                                + "others could not."
                )
        );

        page.append("\n\n");

        page.append(
                Component.literal(
                        "Their fate is unknown."
                )
        );

        page.append("\n\n");

        page.append(
                destinationLink(
                        "Way to Eldoria",
                        "eldoria"
                )
        );

        return page;
    }


    // ==================================================
    // PAGE 4 — VALDREN
    // ==================================================

    private static Component createValdrenPage() {

        MutableComponent page =
                Component.empty();

        page.append(
                Component.literal(
                        "KINGDOM OF VALDREN"
                ).withStyle(
                        ChatFormatting.BOLD
                )
        );

        page.append("\n\n");

        page.append(
                Component.literal(
                        "A kingdom of the\n"
                                + "rejected and forgotten."
                )
        );

        page.append("\n\n");

        page.append(
                Component.literal(
                        "They survived through\n"
                                + "unity and knowledge."
                )
        );

        page.append("\n\n");

        page.append(
                Component.literal(
                        "They stood together."
                )
        );

        page.append("\n\n");

        page.append(
                destinationLink(
                        "Way to Valdren",
                        "valdren"
                )
        );

        return page;
    }


    // ==================================================
    // PAGE 5 — MYRATH
    // ==================================================

    private static Component createMyrathPage() {

        MutableComponent page =
                Component.empty();

        page.append(
                Component.literal(
                        "KINGDOM OF MYRATH"
                ).withStyle(
                        ChatFormatting.BOLD
                )
        );

        page.append("\n\n");

        page.append(
                Component.literal(
                        "A kingdom of fearless\n"
                                + "warriors."
                )
        );

        page.append("\n\n");

        page.append(
                Component.literal(
                        "Courage was their\n"
                                + "greatest virtue."
                )
        );

        page.append("\n\n");

        page.append(
                Component.literal(
                        "They never surrendered."
                )
        );

        page.append("\n\n");

        page.append(
                destinationLink(
                        "Way to Myrath",
                        "myrath"
                )
        );

        return page;
    }


    // ==================================================
    // PAGE 6 — TEMPLE OF GOD
    // ==================================================

    private static Component createTemplePage() {

        MutableComponent page =
                Component.empty();

        page.append(
                Component.literal(
                        "TEMPLE OF GOD"
                ).withStyle(
                        ChatFormatting.BOLD
                )
        );

        page.append("\n\n");

        page.append(
                Component.literal(
                        "An ancient temple\n"
                                + "linked to the four\n"
                                + "kingdoms."
                )
        );

        page.append("\n\n");

        page.append(
                Component.literal(
                        "Its purpose was lost\n"
                                + "to time."
                )
        );

        page.append("\n\n");

        page.append(
                Component.literal(
                        "Something remains\n"
                                + "within."
                )
        );

        page.append("\n\n");

        page.append(
                destinationLink(
                        "Way to the Temple",
                        "temple_of_god"
                )
        );

        return page;
    }


    // ==================================================
    // PAGE 7 — THE CORE
    // ==================================================

    private static Component createCorePage() {

        MutableComponent page =
                Component.empty();

        page.append(
                Component.literal(
                        "THE CORE"
                ).withStyle(
                        ChatFormatting.BOLD
                )
        );

        page.append("\n\n");

        page.append(
                Component.literal(
                        "An ancient artifact\n"
                                + "of the arenas."
                )
        );

        page.append("\n\n");

        page.append(
                Component.literal(
                        "Place it upon a\n"
                                + "pedestal to awaken\n"
                                + "what lies within."
                )
        );

        page.append("\n\n");

        page.append(
                Component.literal(
                        "Its purpose is known\n"
                                + "only to those who\n"
                                + "seek the arenas."
                )
        );

        return page;
    }

    // ==================================================
    // PAGE 8 — CORE CRAFTING
    // ==================================================

    private static Component createCoreCraftingPage() {

        MutableComponent page =
                Component.empty();

        page.append(
                Component.literal(
                        "THE CORE"
                ).withStyle(
                        ChatFormatting.BOLD
                )
        );

        page.append("\n\n");

        page.append(
                Component.literal(
                        "Crafting:"
                ).withStyle(
                        ChatFormatting.BOLD
                )
        );

        page.append("\n\n");

        page.append(
                Component.literal(
                        "O G O\n"
                                + "G T G\n"
                                + "O G O"
                )
        );

        page.append("\n\n");

        page.append(
                Component.literal(
                        "O = Obsidian\n"
                                + "G = Black Stained Glass\n"
                                + "T = Totem"
                )
        );

        page.append("\n\n");

        page.append(
                Component.literal(
                        "Do not lose it."
                ).withStyle(
                        ChatFormatting.BOLD
                )
        );

        return page;
    }

    // ==================================================
    // DESTINATION LINK
    // ==================================================

    private static MutableComponent destinationLink(
            String displayName,
            String structureId
    ) {

        Identifier destinationId =
                Identifier.fromNamespaceAndPath(
                        CrownsOfDust.MOD_ID,
                        structureId
                );

        return Component.literal(
                        "→ " + displayName
                )
                .withStyle(style -> style

                        .withColor(0x5555AA)

                        .withUnderlined(true)

                        .withClickEvent(
                                new ClickEvent.Custom(
                                        destinationId,
                                        Optional.empty()
                                )
                        )

                        .withHoverEvent(
                                new net.minecraft.network.chat.HoverEvent.ShowText(
                                        Component.literal(
                                                "Follow the compass"
                                        )
                                )
                        )
                );
    }


    // ==================================================
    // HANDLE DESTINATION CLICK
    // ==================================================

    @Override
    protected boolean handleClickEvent(
            ClickEvent event
    ) {

        if (event instanceof ClickEvent.Custom custom) {

            // --------------------------------------------------
            // Only accept our own custom click events
            // --------------------------------------------------

            if (!custom.id()
                    .getNamespace()
                    .equals(CrownsOfDust.MOD_ID)) {

                return false;
            }

            // --------------------------------------------------
            // Get structure ID
            // --------------------------------------------------

            String structureId =
                    custom.id().getPath();

            // --------------------------------------------------
            // Send destination to server
            // --------------------------------------------------

            onDestinationSelected(
                    structureId
            );

            return true;
        }

        return super.handleClickEvent(event);
    }


    // ==================================================
    // SEND DESTINATION TO SERVER
    // ==================================================

    private void onDestinationSelected(
            String structureId
    ) {

        Identifier id =
                Identifier.fromNamespaceAndPath(
                        CrownsOfDust.MOD_ID,
                        structureId
                );

        ClientPacketDistributor.sendToServer(
                new ModNetwork.DestinyCompassPayload(
                        id
                )
        );

        // --------------------------------------------------
        // Close book
        // --------------------------------------------------

        Minecraft.getInstance()
                .gui.setScreen(null);
    }


    // ==================================================
    // DON'T PAUSE GAME
    // ==================================================

    @Override
    public boolean isPauseScreen() {

        return false;
    }
}