package com.phantom.cod.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;

import java.util.List;

public class JournalHelper {

    private static final String AUTHOR = "PhantomGG";

    // ==================================================
    // Journal I — The First Record
    // ==================================================

    public static ItemStack createFirstRecord() {

        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);

        List<Filterable<Component>> pages = List.of(

                // ==================================================
                // Opening
                // ==================================================

                page("""
                        I have spent many years searching these lands.

                        Ruins have become my companions.

                        Broken stones, buried halls, forgotten roads...
                        """),

                page("""
                        These are all that remain of the people who once called this place home.

                        There are few records left.

                        Most were destroyed.
                        """),

                page("""
                        Some were lost to the years.

                        And some...

                        I suspect were never meant to be found.
                        """),

                page("""
                        From what I have gathered, this land was not always divided.

                        There was a time before kingdoms.

                        Before borders.

                        Before armies marched beneath different banners.
                        """),

                page("""
                        People lived together.

                        They ate together.

                        They travelled together.

                        They built their homes and shared what they had.

                        There was little reason to fear one's neighbour.
                        """),

                page("""
                        At least...

                        that is what the oldest records claim.

                        Something changed.
                        """),

                page("""
                        Gold became valuable.

                        Rare materials became valuable.

                        Ancient treasures became valuable.

                        Things that had once been nothing more than things became possessions.
                        """),

                page("""
                        And possessions became something worth killing for.

                        I have read enough records to know where that road eventually led.

                        Arguments became conflicts.

                        Conflicts became battles.

                        Battles became wars.
                        """),

                page("""
                        And the people who once shared the same land divided themselves into four kingdoms.

                        Four different ways of surviving the same world.
                        """),

                // ==================================================
                // Ashenreach
                // ==================================================

                page("""
                        ASHENREACH

                        The first name appears more often than any other.

                        A kingdom of strength.
                        """),

                page("""
                        Its people believed power was the answer to every problem.

                        They built armies.

                        They conquered.
                        """),

                page("""
                        They accumulated wealth until there was almost nothing left to take.

                        Then there was...
                        """),

                // ==================================================
                // Eldoria
                // ==================================================

                page("""
                        ELDORIA

                        The scholars.

                        They believed knowledge could accomplish what armies could not.
                        """),

                page("""
                        They recorded everything.

                        Every discovery.

                        Every battle.

                        Every strange object brought through their gates.
                        """),

                page("""
                        Perhaps that is why their ruins frighten me more than the others.

                        I do not yet know why.

                        But I think I will.
                        """),

                // ==================================================
                // Valdren
                // ==================================================

                page("""
                        VALDREN

                        The rejected.

                        People who had been pushed aside by the others.

                        They possessed little.
                        """),

                page("""
                        No great armies.

                        No grand libraries.

                        No great treasures.

                        But they had one thing the others had forgotten.
                        """),

                page("""
                        Each other.

                        And finally...
                        """),

                // ==================================================
                // Myrath
                // ==================================================

                page("""
                        MYRATH

                        Warriors.

                        They were said to be incapable of surrender.
                        """),

                page("""
                        Their old inscriptions repeat the same belief again and again:

                        A warrior may fall...

                        but never while another still stands.
                        """),

                page("""
                        I found that phrase carved into more than one ruin.

                        I wonder why.
                        """),

                // ==================================================
                // The Four Kingdoms
                // ==================================================

                page("""
                        Four kingdoms.

                        Four different ways of surviving the same world.

                        And yet all four are gone.
                        """),

                page("""
                        That is the part I cannot explain.

                        I expected to find evidence of war.

                        And I did.
                        """),

                page("""
                        Burned settlements.

                        Broken weapons.

                        Collapsed fortresses.

                        But war does not explain everything.
                        """),

                page("""
                        There are places where entire settlements were simply abandoned.

                        No bodies.

                        No graves.

                        No signs of battle.
                        """),

                page("""
                        Just empty homes.

                        As though everyone simply...

                        left.
                        """),

                // ==================================================
                // The Older Civilization
                // ==================================================

                page("""
                        The further I travel, the less I understand.

                        For years I believed the four kingdoms were the beginning of this world's history.

                        I was wrong.
                        """),

                page("""
                        There are ruins beneath their ruins.

                        Stones beneath their foundations.

                        Roads that existed long before the first banners of Ashenreach were raised.
                        """),

                page("""
                        Someone lived here before them.

                        Not Ashenreach.

                        Not Eldoria.

                        Not Valdren.

                        Not Myrath.
                        """),

                page("""
                        Someone else.

                        I have found no name for them.

                        No king.

                        No language.
                        """),

                page("""
                        No account of their disappearance.

                        Only remnants.

                        And the strange symbols they left behind.
                        """),

                page("""
                        The four kingdoms built their civilizations upon the remains of something much older.

                        Perhaps they did not know.

                        Or perhaps...

                        they did.
                        """),

                // ==================================================
                // The Inscription
                // ==================================================

                page("""
                        I found an inscription today beneath the ruins.

                        It was older than any of the kingdoms.

                        I copied it before the stone crumbled.
                        """),

                page("""
                        There were only five words.
                        """),

                page("""
                        WE WERE NOT THE FIRST TO FIND IT.
                        """),

                page("""
                        I have no idea what "it" means.

                        But I have seen the same word carved elsewhere.
                        """),

                page("""
                        Always scratched out.

                        Always beneath the names of the four kingdoms.

                        I think someone was trying to erase it.
                        """),

                // ==================================================
                // The Core
                // ==================================================

                page("""
                        Before you continue, there is something I must leave you.

                        Here is the stone that might help you.
                        """),

                page("""
                        I do not know what it is capable of yet.

                        But I believe it may be important.

                        Do not lose it.
                        """),

                // ==================================================
                // Ending
                // ==================================================

                page("""
                        Tomorrow I will travel to the ruins of Ashenreach.

                        Their records may tell me what happened.

                        If the stories are true, their king left one final account before the kingdom fell.
                        """),

                page("""
                        I intend to find it.

                        I only hope I am not too late.

                        I have the uncomfortable feeling that something else has already begun looking for it.
                        """)
        );

        WrittenBookContent content = new WrittenBookContent(
                Filterable.passThrough("The First Record"),
                AUTHOR,
                0,
                pages,
                true
        );

        book.set(DataComponents.WRITTEN_BOOK_CONTENT, content);

        return book;
    }

    // ==================================================
    // Page Helper
    // ==================================================

    private static Filterable<Component> page(String text) {
        return Filterable.passThrough(
                Component.literal(text.trim())
        );
    }
}