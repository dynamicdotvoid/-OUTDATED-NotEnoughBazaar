package dev.nulloverload.notenoughbazaar.utilities;

import net.minecraft.util.EnumChatFormatting;

public class MessageUtils {
	public static String helpMsg = EnumChatFormatting.WHITE+"aliases: "+EnumChatFormatting.RED+"neb, notenoughbazaar\n\n"+EnumChatFormatting.YELLOW+"Commands"+EnumChatFormatting.WHITE+":\n"+EnumChatFormatting.AQUA+"/neb list"+EnumChatFormatting.WHITE+": Lists all current bazaar orders.\n"+EnumChatFormatting.AQUA+"/neb clear"+EnumChatFormatting.WHITE+": Clears the current list of bazaar orders.\n"+EnumChatFormatting.AQUA+"/neb key"+EnumChatFormatting.WHITE+": Sets your api key.\n";
	public static String startupMsg = EnumChatFormatting.GREEN+"It appears that this is your "+EnumChatFormatting.YELLOW+"first "+EnumChatFormatting.GREEN+"time using this mod! Make sure to set your api key and learn the commands using the help list below:";
	public static String nebSignature = "\n"+EnumChatFormatting.GREEN+"["+EnumChatFormatting.YELLOW+"NotEnoughBazaar"+EnumChatFormatting.GREEN+"]\n";
	public static String apiKeyNotSet = nebSignature+(EnumChatFormatting.RED+"For the mod to correctly work, set your api key using /neb key.");
}