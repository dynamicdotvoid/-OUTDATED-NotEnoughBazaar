package dev.nulloverload.notenoughbazaar.utilities;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import dev.nulloverload.notenoughbazaar.NotEnoughBazaar;
import dev.nulloverload.notenoughbazaar.handlers.OrderListHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class UpdateUtils {
	public static void sendOrderUpdate(JSONObject order, String newState) {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if(newState == "OVERBID") {
			order.put("topOrder", false);
			order.put("matchedOrder", false);
			order.put("overbidOrder", true);
			player.addChatMessage(new ChatComponentText(EnumChatFormatting.LIGHT_PURPLE+order.getString("type").toUpperCase()+EnumChatFormatting.DARK_PURPLE+"("+EnumChatFormatting.YELLOW+order.getInt("startingAmount")+EnumChatFormatting.YELLOW+"x "+EnumChatFormatting.LIGHT_PURPLE+order.getString("productID")+EnumChatFormatting.DARK_PURPLE+") - "+EnumChatFormatting.YELLOW+"OVERBID"+EnumChatFormatting.DARK_PURPLE+"("+EnumChatFormatting.LIGHT_PURPLE+order.getDouble("pricePer")+EnumChatFormatting.DARK_PURPLE+")"));	
		}
		if(newState == "RESTORED") {
			order.put("topOrder", true);
			order.put("matchedOrder", false);
			order.put("overbidOrder", false);
			player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN+order.getString("type").toUpperCase()+EnumChatFormatting.DARK_GREEN+"("+EnumChatFormatting.YELLOW+order.getInt("startingAmount")+EnumChatFormatting.YELLOW+"x "+EnumChatFormatting.GREEN+order.getString("productID")+EnumChatFormatting.DARK_GREEN+") - "+EnumChatFormatting.YELLOW+"RESTORED"+EnumChatFormatting.DARK_GREEN+"("+EnumChatFormatting.GREEN+order.getDouble("pricePer")+EnumChatFormatting.DARK_GREEN+")"));	
		}
		if(newState == "MATCHED") {
			order.put("topOrder", false);
			order.put("matchedOrder", true);
			order.put("overbidOrder", false);
			player.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA+order.getString("type").toUpperCase()+EnumChatFormatting.DARK_AQUA+"("+EnumChatFormatting.YELLOW+order.getInt("startingAmount")+EnumChatFormatting.YELLOW+"x "+EnumChatFormatting.AQUA+order.getString("productID")+EnumChatFormatting.DARK_AQUA+") - "+EnumChatFormatting.YELLOW+"MATCHED"+EnumChatFormatting.DARK_AQUA+"("+EnumChatFormatting.AQUA+order.getDouble("pricePer")+EnumChatFormatting.DARK_AQUA+")"));	
		}
	}
	public static void checkOrderUpdate() {		
		JSONObject bazaarProd = ApiUtils.getBazaarProducts().getJSONObject("products");
		Iterator<String> keys = bazaarProd.keys();

		while(keys.hasNext()) {
		    String key = keys.next();
		    if (bazaarProd.get(key) instanceof JSONObject) {
		          JSONObject item = bazaarProd.getJSONObject(key);
		          String itemID = item.getString("product_id");
		          JSONArray buySum = item.getJSONArray("sell_summary");
		          JSONArray sellSum = item.getJSONArray("buy_summary");
		          
		          for(int i = 0; i < OrderListHandler.orders.length(); ++i) {
		        	  JSONObject order = OrderListHandler.orders.getJSONObject(i);
		        	  String orderID = NotEnoughBazaar.bazaarConversionsReversed.getString(order.getString("productID"));
		        	  if(!order.getBoolean("filledOrder")) {
		        		  if(itemID.equalsIgnoreCase(orderID)) {
				        	  if(order.getString("type")=="Buy") {
				        		  double topAmount = buySum.getJSONObject(0).getDouble("pricePerUnit");
				        		  double difference = topAmount - order.getDouble("pricePer");
				        		  if(difference > 0 && !order.getBoolean("overbidOrder")) {
				        			  sendOrderUpdate(order, "OVERBID");
				        		  }
				        		  else if(difference == 0 && !order.getBoolean("topOrder") && buySum.getJSONObject(0).getInt("orders") == 1) {
				        			  sendOrderUpdate(order, "RESTORED");
				        		  }
				        		  else if(difference == 0 && buySum.getJSONObject(0).getInt("orders") > 1 && order.getBoolean("topOrder")) {
				        			  sendOrderUpdate(order, "MATCHED");
				        		  }
				        	  }
				        	  else {
				        		  double topAmount = sellSum.getJSONObject(0).getDouble("pricePerUnit");
				        		  double difference = topAmount - order.getDouble("pricePer");
				        		  if(difference < 0 && !order.getBoolean("overbidOrder")) {
				        			  sendOrderUpdate(order, "OVERBID");
				        		  }
				        		  else if(difference == 0 && !order.getBoolean("topOrder") && sellSum.getJSONObject(0).getInt("orders") == 1) {
				        			  sendOrderUpdate(order, "RESTORED");
				        		  }
				        		  else if(difference == 0 && sellSum.getJSONObject(0).getInt("orders") > 1 && order.getBoolean("topOrder")) {
				        			  sendOrderUpdate(order, "MATCHED");
				        		  }
				        	  }
		        		  }
		        	  }
		          }
		    }
		}
	}
}
