package com.github.theFrameworkItems;

import com.github.theFramework.TheFramework;
import com.github.theFramework.commands.CommandsManager;
import com.github.theFramework.managers.TextManager;
import com.github.theFrameworkItems.commands.GetCommand;
import com.github.theFrameworkItems.managers.ItemListener;
import com.github.theFrameworkItems.managers.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class TheFrameworkItems extends JavaPlugin {

	@Override
	public void onEnable() {
		for (Player player : Bukkit.getOnlinePlayers())
			ItemManager.reload(player);

		TheFramework.registerEvent(new ItemListener());
		CommandsManager.register(new GetCommand(), this);

		TextManager.console(TextManager.format(
			"&b\n\n" +
				"  █ ▀█▀ █▀▀ █▀▄▀█ █▀\n" +
				"  █  █  ██▄ █ ▀ █ ▄█\n" +
				"              &3v&f1.0.9" +
				"\n"
		));
	}

	@Override
	public void onDisable() {
		TextManager.console(TextManager.format("&c[The Framework] Items is offline."));
	}
}
