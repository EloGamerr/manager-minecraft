package fr.elogamerr.manager.files;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemFile
{
    Material material;
    int amount;
    short damage;
    String name;
    String[] lore;
    HashMap<String, Integer> enchantments;
    HashMap<String, Integer> storedEnchantments;
    private transient ItemStack item;

    public ItemFile(Material material, int amount, short damage, String name, String[] lore, HashMap<String, Integer> enchantments, HashMap<String, Integer> storedEnchantments)
    {
        this.material = material;
        this.amount = amount;
        this.damage = damage;
        this.name = name;
        this.lore = lore;
        this.enchantments = enchantments;
        this.storedEnchantments = storedEnchantments;

        this.item = this.toItem();
    }

    /**
     *
     * @param item Utiliser l'item builder pour créer l'itemstack
     */
    public ItemFile(ItemStack item)
    {
        if(item == null)
        {
            return;
        }

        this.material = item.getType();
        this.amount = item.getAmount();
        this.damage = item.getDurability();

        ItemMeta meta = item.getItemMeta();

        if(meta == null)
        {
            return;
        }

        if(meta.getDisplayName() != null)
        {
            this.name = meta.getDisplayName().replaceAll("(§([a-z0-9]))", "&$2");
        }
        if(meta.hasLore())
        {
            String[] newLore = new String[meta.getLore().size()];
            for(int i = 0; i < meta.getLore().size(); i++)
            {
                newLore[i] = meta.getLore().get(i).replaceAll("(§([a-z0-9]))", "&$2");
            }
            this.lore = newLore;
        }


        if(meta.hasEnchants())
        {
            this.enchantments = new HashMap<String, Integer>();
            for(Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet())
            {
                if(entry.getKey() != null)
                    this.enchantments.put(entry.getKey().getName(), entry.getValue());
            }
        }

        if(meta instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) meta;
            if(storageMeta.hasStoredEnchants()) {
                this.storedEnchantments = new HashMap<String, Integer>();
                for(Map.Entry<Enchantment, Integer> entry : storageMeta.getStoredEnchants().entrySet())
                {
                    if(entry.getKey() != null)
                        this.storedEnchantments.put(entry.getKey().getName(), entry.getValue());
                }
            }
        }

        this.item = this.toItem();
    }

    private ItemStack toItem()
    {
        ItemStack is;
        if(this.material != null)
            is = new ItemStack(this.material,1);
        else
            is = new ItemStack(Material.AIR);

        is.setAmount(this.amount);

        is.setDurability(this.damage);

        ItemMeta im = is.getItemMeta();

        if(this.name != null)
            im.setDisplayName(this.name.replaceAll("(&([a-z0-9]))", "\u00A7$2").replace("&&", "&"));

        if(this.lore != null)
        {
            List<String> newLore = new ArrayList<String>();
            for(String lor : this.lore)
            {
                newLore.add(lor.replaceAll("(&([a-z0-9]))", "\u00A7$2").replace("&&", "&"));
            }
            im.setLore(newLore);
        }

        if(this.enchantments != null)
        {
            for(Map.Entry<String, Integer> entry : this.enchantments.entrySet())
            {
                im.addEnchant(Enchantment.getByName(entry.getKey()), entry.getValue(), true);
            }
        }

        if(im instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) im;
            if(this.storedEnchantments != null) {
                for(Map.Entry<String, Integer> entry : this.storedEnchantments.entrySet())
                {
                    storageMeta.addStoredEnchant(Enchantment.getByName(entry.getKey()), entry.getValue(), true);
                }
            }
        }

        is.setItemMeta(im);

        return is;
    }

    public ItemStack getItemStack()
    {
        if(this.item == null)
        {
            this.item = this.toItem();
        }

        return this.item;
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public short getDamage() {
        return damage;
    }

    public String getName() {
        return name;
    }

    public String[] getLore() {
        return lore;
    }

    public HashMap<String, Integer> getEnchantments() {
        return enchantments;
    }

    public HashMap<String, Integer> getStoredEnchantments() {
        return storedEnchantments;
    }
}
