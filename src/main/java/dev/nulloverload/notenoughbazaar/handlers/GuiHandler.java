package dev.nulloverload.notenoughbazaar.handlers;

import dev.nulloverload.notenoughbazaar.NotEnoughBazaar;
import dev.nulloverload.notenoughbazaar.utilities.MessageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GuiHandler {
	@SubscribeEvent
	public void guiOpened(GuiOpenEvent event) {
		if(event.gui instanceof GuiChest) {
			if(((GuiChest)event.gui).inventorySlots instanceof ContainerChest){
				GuiChest bazaarChest = (GuiChest)event.gui;
				ContainerChest invSlots = (ContainerChest)bazaarChest.inventorySlots;
				IInventory lowerChest = invSlots.getLowerChestInventory();
				String chestName = StringUtils.stripControlCodes(lowerChest.getDisplayName().getUnformattedText());
				
				if(chestName.startsWith("Bazaar")) {
					if(NotEnoughBazaar.apiKey!="") {
						NotEnoughBazaar.inBazaar = true;
						NotEnoughBazaar.inConfirmation = false;
						NotEnoughBazaar.confirmChest = null;
					}
					else {
						Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(MessageUtils.apiKeyNotSet));
					}
				}
				else if(chestName.startsWith("Confirm Buy Order") || chestName.startsWith("Confirm Sell Offer")) {
					NotEnoughBazaar.inBazaar = true;
					NotEnoughBazaar.inConfirmation = true;
					NotEnoughBazaar.confirmChest = lowerChest;
				}
				else if(!chestName.startsWith("At what price are you selling?") || !chestName.startsWith("How many do you want?") || !chestName.startsWith("How much do you want to pay?") || !chestName.contains("Bazaar Orders") || !chestName.contains("➜")) {
					NotEnoughBazaar.inBazaar = true;
					NotEnoughBazaar.inConfirmation = false;
					NotEnoughBazaar.confirmChest = null;
				}
				else {
					NotEnoughBazaar.inBazaar = false;
					NotEnoughBazaar.inConfirmation = false;
					NotEnoughBazaar.confirmChest = null;
				}
			}
		}
	}
}
