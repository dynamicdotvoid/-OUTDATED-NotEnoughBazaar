package dev.nulloverload.notenoughbazaar.commands;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

import dev.nulloverload.notenoughbazaar.NotEnoughBazaar;
import dev.nulloverload.notenoughbazaar.handlers.FileHandler;
import dev.nulloverload.notenoughbazaar.handlers.OrderListHandler;
import dev.nulloverload.notenoughbazaar.handlers.ScoreboardHandler;
import dev.nulloverload.notenoughbazaar.utilities.ApiUtils;
import dev.nulloverload.notenoughbazaar.utilities.MessageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;

public class NotEnoughBazaarCommand extends CommandBase {
	private final List<String> aliases = Lists.newArrayList(dev.nulloverload.notenoughbazaar.NotEnoughBazaar.MODID, "neb", "notenoughbazaar");
	public static JSONArray sortedNpcList = new JSONArray();
	public static String jsonToCopy = "";
	@Override
	public String getCommandName() {
		return "notenoughbazaar";
	}
	@Override
	public boolean canCommandSenderUseCommand(final ICommandSender sender) {
		return true;
	}
	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/notenoughbazaar [subcommand]";
	}
    @Override
    public List<String> getCommandAliases() {
        return aliases;
    }
	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if(args.length >= 1 && args[0].equalsIgnoreCase("list")) {
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < OrderListHandler.orders.length(); ++i) {
				JSONObject order = OrderListHandler.orders.getJSONObject(i);
				String orderState = "";
				
				if(order.getBoolean("overbidOrder"))orderState="Overbid";
				else if(order.getBoolean("topOrder"))orderState="Top";
				else if(order.getBoolean("matchedOrder"))orderState="Matched";
				else if(order.getBoolean("filledOrder"))orderState="Filled";
				
				String orderDisplay = order.getString("type")+"("+order.getInt("startingAmount")+"x "+order.getString("productID")+") - "+orderState;
				
				sb.append(EnumChatFormatting.AQUA+orderDisplay+"\n");
			}
			
			player.addChatMessage(new ChatComponentText(MessageUtils.nebSignature+"\n"+sb.toString()));
		}
		else if(args.length >= 1 && args[0].equalsIgnoreCase("listraw")) {
			player.addChatMessage(new ChatComponentText(OrderListHandler.orders.toString()));
		}
		else if(args.length >= 1 && args[0].equalsIgnoreCase("help")) {
			player.addChatMessage(new ChatComponentText(MessageUtils.nebSignature+MessageUtils.helpMsg));
		}
		else if(args.length >= 1 && args[0].equalsIgnoreCase("clear")) {
			OrderListHandler.orders = new JSONArray();
			player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN+"Cleared current list of bazaar orders!"));
		}
		else if(args.length >=1 && args[0].equalsIgnoreCase("key")){
			if(args.length == 2) {
				if(ApiUtils.validateKey(args[1])) {
					FileHandler.setApiKey(args[1]);
					player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN+"Successfuly set api key!"));
				}
				else {
					player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED+"Invalid api key! Make sure that the key was typed in correctly, or type /api new for a new key."));
				}
			}
			else {
				player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED+"run /neb key [api key] to set your api key, run /api to view your current api key."));
			}
		}
		else if(args.length >= 1 && args[0].equalsIgnoreCase("npclistreload")) {
			if(NotEnoughBazaar.apiKey=="")Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(MessageUtils.apiKeyNotSet));
			else {
				Runnable backGroundRunnable = new Runnable() {
				    public void run(){
				         getNpcList();
				    }};
				Thread npcThread = new Thread(backGroundRunnable);
				npcThread.start();
			}
		}
		else if(args.length >= 1 && args[0].equalsIgnoreCase("npclist")) {
			displayNpcList();
		}
		else if(args.length >= 1 && args[0].equalsIgnoreCase("npclistjson")) {
		}
		else if(args.length >= 1 && args[0].equalsIgnoreCase("copyjson")) {
			Toolkit tk = Toolkit.getDefaultToolkit();
			Clipboard board = tk.getSystemClipboard();
			StringSelection ss = new StringSelection(jsonToCopy);
			board.setContents(ss, null);
			jsonToCopy = "";
		}
		else if(args.length >= 1 && args[0].equalsIgnoreCase("devhelp")) {
			player.addChatMessage(new ChatComponentText(MessageUtils.nebSignature+MessageUtils.devHelpMsg));
		}
		else if(args.length >= 1 && args[0].equalsIgnoreCase("npcbaz")) {
			if(args.length >= 2) {
				JSONObject bazaarProd = ApiUtils.getBazaarProducts().getJSONObject("products");
				Iterator<String> keys = NotEnoughBazaar.npcConversions.keys();
				
				StringBuilder sbb = new StringBuilder();
				sbb.append(MessageUtils.nebSignature+EnumChatFormatting.YELLOW+"Npc -> Baz"+EnumChatFormatting.GREEN+"\nItem : Amount : Profit\n\n");
				
				while(keys.hasNext()) {
				    String key = keys.next();
				    if (NotEnoughBazaar.npcConversions.getString(key) instanceof String) {
				    	JSONObject bazaarItem = bazaarProd.getJSONObject(key);
				    	if(NotEnoughBazaar.npcConversions.has(bazaarItem.getString("product_id"))) {
					    	double npcPrice = Double.parseDouble(NotEnoughBazaar.npcConversions.getString(key));
					    	double totalPrice = npcPrice*640;
					    	if(args[1].equalsIgnoreCase("instant")) {
					    		JSONArray sum = bazaarItem.getJSONArray("sell_summary");
					    		if(!sum.isEmpty()) {
						    		JSONObject first = sum.getJSONObject(0);
						    		double pricePer = first.getDouble("pricePerUnit");
						    		int estimatedProfit = (int) (pricePer*640);
						    		
						    		sbb.append(EnumChatFormatting.AQUA+NotEnoughBazaar.bazaarConversions.getString(key)+EnumChatFormatting.WHITE+" : "+EnumChatFormatting.AQUA+"640 "+EnumChatFormatting.WHITE+": "+EnumChatFormatting.AQUA+estimatedProfit+"\n");
					    		}
					    	}
					    	else if(args[1].equalsIgnoreCase("order")) {
					    		JSONArray sum = bazaarItem.getJSONArray("buy_summary");
					    		if(!sum.isEmpty()) {
						    		JSONObject first = sum.getJSONObject(0);	
						    		double pricePer = first.getDouble("pricePerUnit")-.1;
						    		int estimatedProfit = (int) ((pricePer*640)-totalPrice);
						    		
						    		
						    		sbb.append(EnumChatFormatting.AQUA+NotEnoughBazaar.bazaarConversions.getString(key)+EnumChatFormatting.WHITE+" : "+EnumChatFormatting.AQUA+"640 "+EnumChatFormatting.WHITE+": "+EnumChatFormatting.AQUA+estimatedProfit+"\n");
					    		}
					    	}	
				    	}
				    }
				}
				
				player.addChatMessage(new ChatComponentText(sbb.toString()));
				
			}
			else {
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED+"Invalid Usage! Use /neb npcbaz [instant/order], using instant or order if you want to get prices for either."));
			}
		}
		else if(args.length >= 1 && args[0].equalsIgnoreCase("baznpc")) {
			if(args.length >= 4) {
				JSONObject bazaarProd = ApiUtils.getBazaarProducts().getJSONObject("products");
				Iterator<String> keys = NotEnoughBazaar.npcConversions.keys();
				
				StringBuilder sbr = new StringBuilder();
				sbr.append(MessageUtils.nebSignature+EnumChatFormatting.YELLOW+"Baz -> Npc"+EnumChatFormatting.GREEN+"\nItem : Amount : Profit\n\n");
				
				while(keys.hasNext()) {
				    String key = keys.next();
				    if (NotEnoughBazaar.npcConversions.getString(key) instanceof String) {
				    	JSONObject bazaarItem = bazaarProd.getJSONObject(key);
				    	if(NotEnoughBazaar.npcConversions.has(bazaarItem.getString("product_id"))) {
					    	double npcPrice = Double.parseDouble(NotEnoughBazaar.npcConversions.getString(key));
					    	if(args[2].equalsIgnoreCase("instant")) {
					    		JSONArray sum = bazaarItem.getJSONArray("buy_summary");
					    		if(!sum.isEmpty()) {
					    			JSONObject first = sum.getJSONObject(0);
					    			double pricePer = first.getDouble("pricePerUnit");
					    			int amountToBuy = 0;
					    			if(!args[3].equalsIgnoreCase("purse")) {
					    				amountToBuy = Integer.parseInt(args[3]);
					    			}
					    			else {
					    				List<String> scoreboardLines = ScoreboardHandler.getSidebarLines();
					    				String cleanCoin = StringUtils.stripControlCodes(scoreboardLines.get(2)).replaceAll(",", "");
					    				double purseAmount = Double.parseDouble(cleanCoin);
					    				amountToBuy = (int) (purseAmount/pricePer);
					    			}
					    			int estimatedProfit = 0;
					    			if(!args[1].equalsIgnoreCase("margin")) {
					    				estimatedProfit = (int) ((((npcPrice*amountToBuy)-(pricePer*amountToBuy)))*1.04);
					    			}
					    			else {
					    				estimatedProfit = (int) (((npcPrice*amountToBuy)-(pricePer*amountToBuy))/(pricePer*amountToBuy));
					    			}
					    			if(estimatedProfit > 1) {
					    				sbr.append(EnumChatFormatting.AQUA+NotEnoughBazaar.bazaarConversions.getString(key)+EnumChatFormatting.WHITE+" : "+EnumChatFormatting.AQUA+amountToBuy+EnumChatFormatting.WHITE+" : "+EnumChatFormatting.AQUA+estimatedProfit+"\n");	
					    			}
					    		}
					    	}
					    	else if(args[2].equalsIgnoreCase("order")) {
					    		JSONArray sum = bazaarItem.getJSONArray("sell_summary");
					    		if(!sum.isEmpty()) {
					    			JSONObject first = sum.getJSONObject(0);
					    			double pricePer = first.getDouble("pricePerUnit");
					    			int amountToBuy = 0;
					    			if(!args[3].equalsIgnoreCase("purse")) {
					    				amountToBuy = Integer.parseInt(args[3]);
					    			}
					    			else {
					    				List<String> scoreboardLines = ScoreboardHandler.getSidebarLines();
					    				String cleanCoin = StringUtils.stripControlCodes(scoreboardLines.get(2)).replaceAll(",", "");
					    				double purseAmount = Double.parseDouble(cleanCoin);
					    				amountToBuy = (int) (purseAmount/pricePer);
					    			}
					    			int estimatedProfit = 0;
					    			if(!args[1].equalsIgnoreCase("margin")) {
					    				estimatedProfit = (int) ((npcPrice*amountToBuy)-(pricePer*amountToBuy));
					    			}
					    			else {
					    				estimatedProfit = (int) (((npcPrice*amountToBuy)-(pricePer*amountToBuy))/(pricePer*amountToBuy));
					    			}
					    			sbr.append(EnumChatFormatting.AQUA+NotEnoughBazaar.bazaarConversions.getString(key)+EnumChatFormatting.WHITE+" : "+EnumChatFormatting.AQUA+amountToBuy+EnumChatFormatting.WHITE+" : "+EnumChatFormatting.AQUA+estimatedProfit+"\n");
					    		}
					    	}	
				    	}
				    }
				}
				
				player.addChatMessage(new ChatComponentText(sbr.toString()));
				
			}
			else {
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED+"Invalid Usage! Use /neb baznpc [margin/profit ] [instant/order] [amount/pure], using instant or order if you want to get prices for either, and purse if you want to buy as many items as your purse can allow."));
			}
		}
		else if(args.length >= 1 && args[0].equalsIgnoreCase("board")) {
			List<String> scoreboardLines = ScoreboardHandler.getSidebarLines();
			player.addChatMessage(new ChatComponentText(EnumChatFormatting.LIGHT_PURPLE+scoreboardLines.toString()));
		}
	}
	public void getNpcList() {
		sortedNpcList = new JSONArray();
		try {
			URL url = new URL("https://api.hypixel.net/resources/skyblock/items?key="+NotEnoughBazaar.apiKey);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setRequestMethod("GET");
			con.connect();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line = null;
			
			while((line = br.readLine())!=null) {
				sb.append(line+"\n");
			}
			
			String jsonResponse = sb.toString();
			JSONObject sbItems = new JSONObject(jsonResponse);
			
			JSONArray allItems = sbItems.getJSONArray("items");
			Iterator<String> keys = NotEnoughBazaar.npcConversions.keys();
			
			JSONArray unsortedArray = new JSONArray();
			
			while(keys.hasNext()) {
			    String key = keys.next();
			    if (NotEnoughBazaar.npcConversions.get(key) instanceof JSONObject) {
			          for(int i = 0; i < allItems.length(); ++i) {
			        	  JSONObject sbItem = allItems.getJSONObject(i);
			        	  
			        	  if(key.equalsIgnoreCase(sbItem.getString("id"))) {
			        		  unsortedArray.put(sbItem);
			        	  }
			          }
			    }
			}
			
			List<JSONObject> unsortedList = new ArrayList<JSONObject>();
			for(int i = 0; i < unsortedArray.length(); ++i) {
				unsortedList.add(unsortedArray.getJSONObject(i));
			}
			Collections.sort(unsortedList, new Comparator<JSONObject>() {

			    public int compare(JSONObject a, JSONObject b) {
			        int valA = 0;
			        int valB = 0;

			        try {
			            valA = a.getInt("npc_sell_price");
			            valB = b.getInt("npc_sell_price");
			        } 
			        catch (JSONException e) {
			            e.printStackTrace();
			        }

			        return Integer.compare(valB, valA);
			    }
			});
			for (int i = 0; i < unsortedArray.length(); i++) {
			    sortedNpcList.put(unsortedList.get(i));
			}
			
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(MessageUtils.nebSignature+EnumChatFormatting.GREEN+"Successfully loaded npc items."));
			StringBuilder sb2 = new StringBuilder();
			sb2.append("{\n");
			for(int i = 0; i < sortedNpcList.length(); ++i) {
				JSONObject npcItem = sortedNpcList.getJSONObject(i);
				sb2.append("\t\""+npcItem.getString("id")+"\": \""+npcItem.getInt("npc_sell_price")+"\",\n");
			}
			sb2.append("}");
			jsonToCopy = sb2.toString();
			
			ChatComponentText msg = new ChatComponentText(sb2.toString());
			ChatStyle copy = new ChatStyle();
			copy.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/neb copyjson"));
	    	copy.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GREEN+"Copy data to clipboard")));

			msg.setChatStyle(copy);
			Minecraft.getMinecraft().thePlayer.addChatMessage(msg);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void displayNpcList() {
		JSONObject npcConversions = NotEnoughBazaar.npcConversions;
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		StringBuilder stbr = new StringBuilder();
		stbr.append(MessageUtils.nebSignature);
		stbr.append("Item : Npc Sell Price\n\n");
		Iterator<String> keys = npcConversions.keys();

		while(keys.hasNext()) {
		    String key = keys.next();
		    if (npcConversions.getString(key) instanceof String) {
		          stbr.append(EnumChatFormatting.AQUA+NotEnoughBazaar.bazaarConversions.getString(key)+EnumChatFormatting.WHITE+" : "+EnumChatFormatting.AQUA+npcConversions.getString(key)+"\n");
		    }
		}
		player.addChatMessage(new ChatComponentText(stbr.toString()));
	}
}
