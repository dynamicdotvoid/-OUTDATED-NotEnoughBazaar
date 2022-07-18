package dev.nulloverload.notenoughbazaar.handlers;

import org.json.JSONObject;

import dev.nulloverload.notenoughbazaar.NotEnoughBazaar;
import dev.nulloverload.notenoughbazaar.utilities.MessageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class TickHandler {
	@SubscribeEvent
	public void bazaarTick(TickEvent event) {
		if(NotEnoughBazaar.inConfirmation && NotEnoughBazaar.apiKey != "" && NotEnoughBazaar.confirmChest != null) {
			if (NotEnoughBazaar.confirmChest.getStackInSlot(13) != null && OrderListHandler.orderToVerify == null) {
				parseLore(NotEnoughBazaar.confirmChest);
			}
		}
	}
	public void parseLore(IInventory chest) {
		ItemStack loreItem = chest.getStackInSlot(13);
		NBTTagList loreTag = loreItem.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
		String priceLine = loreTag.getStringTagAt(2);
		String productLine = loreTag.getStringTagAt(4);
		
		String pricePerS = StringUtils.stripControlCodes(priceLine.split(" ")[3].replace(" coins", "").replaceAll(",", ""));
		double pricePer = Double.parseDouble(pricePerS);
		
		String amountS = StringUtils.stripControlCodes(productLine.split(" ")[1].split("x")[0].replaceAll(",", ""));
		int amount = Integer.parseInt(amountS);
		
		String productID = StringUtils.stripControlCodes(productLine.split("x ")[1]);
		
		double totalAmount = (pricePer*amount);
		
		
		if(NotEnoughBazaar.bazaarConversionsReversed.has(productID)) {
			String productIdReversed = NotEnoughBazaar.bazaarConversionsReversed.getString(productID);	
			String productWAmount = StringUtils.stripControlCodes(productLine).split(": ")[1];
			
			OrderListHandler.orderVerifyString[0] = productIdReversed;
			OrderListHandler.orderVerifyString[1] = productWAmount;
			OrderListHandler.orderToVerify = new JSONObject()
					.put("productID", productID)
					.put("startingAmount", amount)
					.put("amountLeft", amount)
					.put("pricePer", pricePer)
					.put("overbidOrder", false)
					.put("matchedOrder", false)
					.put("topOrder", true)
					.put("filledOrder", false)
					.put("orderValue", totalAmount);
			
			String chestName = StringUtils.stripControlCodes(chest.getDisplayName().getUnformattedText());
			if(chestName.startsWith("Confirm Buy Order")) {
				OrderListHandler.orderToVerify.put("type", "Buy");
			}
			else if(chestName.startsWith("Confirm Sell Offer")) {
				OrderListHandler.orderToVerify.put("type", "Sell");
				
				double sellOrderValueU = totalAmount*.989;
				double sellOrderValueR = ((double)((int)(sellOrderValueU *10.0)))/10.0;
				OrderListHandler.orderToVerify.put("orderValue", sellOrderValueR);
			}
		}
		else {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(MessageUtils.nebSignature+EnumChatFormatting.RED+"This item "+productID+EnumChatFormatting.RED+"does not exist in the code. Please tell this to the mod developer"));
		}
	}
}
