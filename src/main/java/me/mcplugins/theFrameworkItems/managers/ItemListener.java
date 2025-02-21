package me.mcplugins.theFrameworkItems.managers;

import me.mcplugins.theframework.managers.TextManager;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemListener implements Listener {

	@EventHandler
	public void onItemDamage(PlayerItemDamageEvent event) {
		ItemStack item = event.getItem();
		if (!item.hasItemMeta()) return;

		CustomItem customItem = ItemManager.getItem(item);
		if (customItem == null || customItem.getLore() != null) return;

		int customDurability = customItem.getDurability();
		int defaultDurability = item.getType().getMaxDurability();
		if (customDurability <= 0 || defaultDurability <= 0) return;

		int scaledDamage = Math.max(0, (defaultDurability / customDurability) * event.getDamage());
		event.setDamage(scaledDamage);

		ItemMeta meta = item.getItemMeta();
		if (!(meta instanceof Damageable damageable)) return;

		int remainingDurability = defaultDurability - (damageable.getDamage() + scaledDamage);
		int customRemaining = remainingDurability / scaledDamage;

		List<Component> lore = meta.lore();
		if (lore == null || lore.isEmpty()) return;

		lore.set(lore.size() - 1, TextManager.format(
			"&8&o" + customItem.getType() + " " + customRemaining + "/" + customDurability));

		meta.lore(lore);
		item.setItemMeta(meta);
	}
}
