package dev.nulloverload.notenoughbazaar.handlers;

import org.json.JSONArray;
import org.json.JSONObject;

import dev.nulloverload.notenoughbazaar.NotEnoughBazaar;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class OrderListHandler {
	public static JSONArray orders = new JSONArray();
	
	public static JSONObject orderToVerify = null;
	public static String[] orderVerifyString = new String[2];
	
	@SubscribeEvent
	public void onClientChatReceive(ClientChatReceivedEvent event) {
		String cleanedMessage = StringUtils.stripControlCodes(event.message.getUnformattedText());
		if(cleanedMessage.startsWith("Buy Order Setup!") || cleanedMessage.startsWith("Sell Offer Setup!")) {
			String extracted0 = StringUtils.stripControlCodes(NotEnoughBazaar.bazaarConversionsReversed.getString(cleanedMessage.split("x ")[1].split(" for")[0]));
			String extracted1 = StringUtils.stripControlCodes(cleanedMessage.split("! ")[1].split(" for")[0]);
			if(orderVerifyString[0]!=null&&orderVerifyString[1]!=null&&
					orderVerifyString[0].equalsIgnoreCase(extracted0)&&
					orderVerifyString[1].equalsIgnoreCase(extracted1)) {
				orders.put(orderToVerify);
				orderToVerify = null;
				orderVerifyString = new String[2];
			}
			else {
				orderToVerify = null;
				orderVerifyString = new String[2];
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED+"An error occurred while listing your offer."));
			}
		}
		else if(cleanedMessage.startsWith("[Bazaar] Your")) {
			for(int i = 0; i < orders.length(); ++i) {
				JSONObject order = orders.getJSONObject(i);
				String filledBuy = "[Bazaar] Your Buy Order for "+order.getInt("startingAmount")+"x "+order.getString("productID");
				String filledSell = "[Bazaar] Your Sell Offer for "+order.getInt("startingAmount")+"x "+order.getString("productID");
				if(cleanedMessage.startsWith(filledBuy) || cleanedMessage.startsWith(filledSell)) {
					order.put("filledOrder", true);
					order.put("topOrder", false);
					order.put("matchedOrder", false);
					order.put("overbidOrder", false);
				}
			}
		}
		else if(cleanedMessage.startsWith("Bazaar! Claimed")) {
			for(int i = 0; i < orders.length(); ++i) {
				JSONObject order = orders.getJSONObject(i);
				String buyClaim = "Bazaar! Claimed "+order.getInt("amountLeft")+"x "+order.getString("productID")+" worth "+order.getDouble("orderValue")+" coins";
				String sellClaim = "Bazaar! Claimed "+order.getDouble("orderValue")+" coins from selling "+order.getInt("amountLeft")+"x "+order.getString("productID");
				if(cleanedMessage.startsWith(buyClaim)||cleanedMessage.startsWith(sellClaim)) {
					orders.remove(i);
				}
				else if(cleanedMessage.contains("selling")) {
					String itemName = cleanedMessage.split("x ")[1].split(" at")[0];
					int amountSold = Integer.parseInt(cleanedMessage.split("selling ")[1].split(" ")[0].replaceAll(",", ""));
					double pricePer = Double.parseDouble(cleanedMessage.split("at ")[1].split(" each")[0].replaceAll(",", ""));
					
					if(pricePer == order.getDouble("pricePer") && itemName == order.getString("productID")) {
						order.put("amountLeft", order.getInt("amountLeft")-amountSold);
						double totalValue = order.getInt("amountLeft")*order.getDouble("pricePer");
						order.put("orderValue", totalValue*.989);
					}
				}
				else if(cleanedMessage.contains("bought for")) {
					String itemName = cleanedMessage.split("x ")[1].split(" worth")[0];
					int amountBought = Integer.parseInt(cleanedMessage.split("Claimed ")[1].split("x")[0].replaceAll(",", ""));
					double pricePer = Double.parseDouble(cleanedMessage.split("for ")[1].split(" each")[0].replaceAll(",", ""));
					
					if(pricePer == order.getDouble("pricePer") && itemName == order.getString("productID")) {
						order.put("amountLeft", order.getInt("amountLeft")-amountBought);
						double totalValue = order.getInt("amountLeft")*order.getDouble("pricePer");
						order.put("orderValue", totalValue);
					}
				}
			}
		}
		else if(cleanedMessage.startsWith("Cancelled! Refunded")) {
			for(int i = 0; i < orders.length(); ++i) {
				JSONObject order = orders.getJSONObject(i);
				if(cleanedMessage.contains("cancelling sell offer")) {
					if(cleanedMessage.contains(order.getInt("amountLeft")+"x "+order.getString("productID"))) {
						orders.remove(i);
					}
				}
				else if(cleanedMessage.contains("cancelling buy order")) {
					if(cleanedMessage.contains(Double.toString(order.getDouble("orderValue")))) {
						orders.remove(i);
					}
				}
			}
		}
	}
}