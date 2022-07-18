package dev.nulloverload.notenoughbazaar.commands;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.Lists;

import dev.nulloverload.notenoughbazaar.handlers.FileHandler;
import dev.nulloverload.notenoughbazaar.handlers.OrderListHandler;
import dev.nulloverload.notenoughbazaar.utilities.ApiUtils;
import dev.nulloverload.notenoughbazaar.utilities.MessageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class NotEnoughBazaarCommand extends CommandBase {
	private final List<String> aliases = Lists.newArrayList(dev.nulloverload.notenoughbazaar.NotEnoughBazaar.MODID, "neb", "notenoughbazaar");
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
		if(args.length >= 1 && args[0].equalsIgnoreCase("listraw")) {
			player.addChatMessage(new ChatComponentText(OrderListHandler.orders.toString()));
		}
		if(args.length >= 1 && args[0].equalsIgnoreCase("help")) {
			player.addChatMessage(new ChatComponentText(MessageUtils.nebSignature+MessageUtils.helpMsg));
		}
		if(args.length >= 1 && args[0].equalsIgnoreCase("clear")) {
			OrderListHandler.orders = new JSONArray();
			player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN+"Cleared current list of bazaar orders!"));
		}
		if(args.length >=1 && args[0].equalsIgnoreCase("key")){
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
	}

}
