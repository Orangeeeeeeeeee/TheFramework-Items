package com.github.theFrameworkItems.commands;

import com.github.theFramework.commands.CustomCommand;
import com.github.theFramework.managers.TextManager;
import com.github.theFrameworkItems.managers.CustomItem;
import com.github.theFrameworkItems.managers.ItemManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GetCommand extends CustomCommand {
	public GetCommand() {
		super("get");
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage(Component.text("Only players can use this command!", NamedTextColor.RED));
			return;
		}

		CustomItem item = ItemManager.getItem(args[0].toLowerCase());
		if (item == null) {
			TextManager.sendActionBar(player, TextManager.format("&cItem not found: &7" + args[0]));
			player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
			return;
		}

		int amount = 1;
		if (args.length > 1) {
			try {
				amount = Math.max(1, Integer.parseInt(args[1]));
			} catch (Exception ignored) {
			}
		}

		player.getInventory().addItem(item.create(amount));
		TextManager.sendTitle(player,
			TextManager.format(item.getName()),
			TextManager.format(item.getRarity()),
			0.2, 1, 0.2
		);
		player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
	}
}