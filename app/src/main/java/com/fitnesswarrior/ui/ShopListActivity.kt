package com.fitnesswarrior.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fitnesswarrior.R
import com.fitnesswarrior.models.*
import com.fitnesswarrior.services.GameManager

// shop screen with grid layout for browsing items
class ShopListActivity : AppCompatActivity() {
    private lateinit var gm: GameManager
    private lateinit var adapter: RecyclerView.Adapter<*>
    private var category = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop_list)
        gm = GameManager.getInstance(this)
        category = intent.getStringExtra("category") ?: "potions"

        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener { finish() }
        updateGold()

        // set title based on category
        val title = when (category) {
            "potions" -> "🧪 POTIONS"
            "shields" -> "🛡️ ARMOR"
            "weapons" -> "⚔️ WEAPONS"
            else -> "SHOP"
        }
        findViewById<TextView>(R.id.tvShopTitle)?.text = title
        findViewById<TextView>(R.id.tvPlayerLevel)?.text = "Your Level: ${gm.player.level}"

        // setup grid recyclerview with 3 columns
        val rv = findViewById<RecyclerView>(R.id.rvShopItems)
        rv.layoutManager = GridLayoutManager(this, 3)

        when (category) {
            "potions" -> {
                val items = ShopCatalog.getPotions()
                adapter = PotionAdapter(items)
                rv.adapter = adapter
            }
            "shields" -> {
                val items = ShopCatalog.getShields()
                adapter = EquipAdapter(items)
                rv.adapter = adapter
            }
            "weapons" -> {
                val items = ShopCatalog.getWeapons()
                adapter = EquipAdapter(items)
                rv.adapter = adapter
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateGold()
    }

    private fun updateGold() {
        findViewById<TextView>(R.id.tvGold)?.text = "${gm.player.gold} ⚡"
    }

    private fun refreshList() {
        updateGold()
        adapter.notifyDataSetChanged()
    }

    // adapter for potions and consumable items
    inner class PotionAdapter(private val potions: List<InventoryItem>) :
        RecyclerView.Adapter<PotionAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val root: View = view.findViewById(R.id.cardRoot)
            val emoji: TextView = view.findViewById(R.id.tvEmoji)
            val name: TextView = view.findViewById(R.id.tvItemName)
            val rarity: TextView = view.findViewById(R.id.tvRarity)
            val stats: TextView = view.findViewById(R.id.tvStats)
            val price: TextView = view.findViewById(R.id.tvPrice)
            val action: TextView = view.findViewById(R.id.btnAction)
            val owned: TextView = view.findViewById(R.id.tvOwned)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_shop_card, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val potion = potions[position]
            val ownedQty = gm.player.inventory.find { it.id == potion.id }?.quantity ?: 0

            holder.emoji.text = potion.emoji
            holder.name.text = potion.name
            holder.stats.text = potion.description
            holder.price.text = "${potion.price} ⚡"
            holder.root.setBackgroundResource(R.drawable.shop_grid_potion)

            // show item type
            val typeLabel = when (potion.type) {
                ItemType.HEALTH_POTION -> "❤️ Health"
                ItemType.MANA_POTION -> "💎 Mana"
                ItemType.ATTACK_ITEM -> "💥 Attack"
                ItemType.BUFF -> "⬆️ Buff"
            }
            holder.rarity.text = typeLabel
            holder.rarity.setTextColor(0xFF4CAF50.toInt())

            // show owned count
            holder.owned.visibility = View.VISIBLE
            holder.owned.text = "x$ownedQty"

            // buy button
            val canAfford = gm.player.gold >= potion.price
            holder.action.text = "BUY"
            holder.action.setBackgroundResource(if (canAfford) R.drawable.btn_buy else R.drawable.btn_locked)
            holder.action.setTextColor(if (canAfford) 0xFF1A1A2E.toInt() else 0xFFCCCCCC.toInt())

            holder.action.setOnClickListener {
                if (gm.buyPotion(potion)) {
                    Toast.makeText(this@ShopListActivity, "Bought ${potion.name}! 🎉", Toast.LENGTH_SHORT).show()
                    refreshList()
                } else {
                    Toast.makeText(this@ShopListActivity, "Need ${potion.price}⚡!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        override fun getItemCount() = potions.size
    }

    // adapter for equipment like weapons and shields
    inner class EquipAdapter(private val items: List<Equipment>) :
        RecyclerView.Adapter<EquipAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val root: View = view.findViewById(R.id.cardRoot)
            val emoji: TextView = view.findViewById(R.id.tvEmoji)
            val name: TextView = view.findViewById(R.id.tvItemName)
            val rarity: TextView = view.findViewById(R.id.tvRarity)
            val stats: TextView = view.findViewById(R.id.tvStats)
            val price: TextView = view.findViewById(R.id.tvPrice)
            val action: TextView = view.findViewById(R.id.btnAction)
            val owned: TextView = view.findViewById(R.id.tvOwned)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_shop_card, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            val isOwned = gm.player.ownedEquipment.any { it.id == item.id }
            val isEquipped = gm.player.equippedWeapon?.id == item.id || gm.player.equippedArmor?.id == item.id
            val levelLocked = gm.player.level < item.levelRequired

            holder.emoji.text = item.emoji
            holder.name.text = item.name
            holder.owned.visibility = View.GONE

            // rarity stars with color
            val stars = "★".repeat(item.rarity.ordinal + 1)
            holder.rarity.text = stars
            holder.rarity.setTextColor(item.rarity.color)

            // short stat line for grid
            val statText = if (item.type == EquipmentType.WEAPON) {
                "+${item.strengthBonus} STR"
            } else {
                "+${item.defenseBonus} DEF"
            }
            holder.stats.text = statText

            // cell border color by rarity
            val cellBg = when (item.rarity) {
                Rarity.COMMON -> R.drawable.shop_grid_common
                Rarity.UNCOMMON -> R.drawable.shop_grid_uncommon
                Rarity.RARE -> R.drawable.shop_grid_rare
                Rarity.EPIC -> R.drawable.shop_grid_epic
                Rarity.LEGENDARY -> R.drawable.shop_grid_legendary
            }
            holder.root.setBackgroundResource(cellBg)

            // button and price based on item status
            when {
                isEquipped -> {
                    holder.price.text = "ACTIVE"
                    holder.price.setTextColor(0xFF4CAF50.toInt())
                    holder.action.text = "EQUIPPED"
                    holder.action.setBackgroundResource(R.drawable.btn_equipped)
                    holder.action.setTextColor(0xFFFFFFFF.toInt())
                    holder.action.setOnClickListener {
                        Toast.makeText(this@ShopListActivity, "Already equipped!", Toast.LENGTH_SHORT).show()
                    }
                }
                isOwned -> {
                    holder.price.text = "OWNED"
                    holder.price.setTextColor(0xFF1976D2.toInt())
                    holder.action.text = "EQUIP"
                    holder.action.setBackgroundResource(R.drawable.btn_equip)
                    holder.action.setTextColor(0xFFFFFFFF.toInt())
                    holder.action.setOnClickListener {
                        gm.equipItem(item)
                        Toast.makeText(this@ShopListActivity, "Equipped ${item.name}! ⚔️", Toast.LENGTH_SHORT).show()
                        refreshList()
                    }
                }
                levelLocked -> {
                    holder.price.text = "Lvl ${item.levelRequired}"
                    holder.price.setTextColor(0xFF999999.toInt())
                    holder.emoji.text = "🔒"
                    holder.action.text = "LOCKED"
                    holder.action.setBackgroundResource(R.drawable.btn_locked)
                    holder.action.setTextColor(0xFFCCCCCC.toInt())
                    holder.action.setOnClickListener {
                        Toast.makeText(this@ShopListActivity, "Reach level ${item.levelRequired} to unlock ${item.name}!", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    holder.price.text = "${item.price} ⚡"
                    holder.price.setTextColor(0xFFFFD700.toInt())
                    val canAfford = gm.player.gold >= item.price
                    holder.action.text = "BUY"
                    holder.action.setBackgroundResource(if (canAfford) R.drawable.btn_buy else R.drawable.btn_locked)
                    holder.action.setTextColor(if (canAfford) 0xFF1A1A2E.toInt() else 0xFFCCCCCC.toInt())
                    holder.action.setOnClickListener {
                        AlertDialog.Builder(this@ShopListActivity)
                            .setTitle("Buy ${item.name}?")
                            .setMessage("${item.description}\n\nCost: ${item.price}⚡\nYour Gold: ${gm.player.gold}⚡")
                            .setPositiveButton("BUY") { _, _ ->
                                if (gm.buyEquipment(item)) {
                                    gm.equipItem(item)
                                    Toast.makeText(this@ShopListActivity, "Bought & equipped ${item.name}! 🎉", Toast.LENGTH_SHORT).show()
                                    refreshList()
                                } else {
                                    Toast.makeText(this@ShopListActivity, "Not enough gold!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                }
            }
        }

        override fun getItemCount() = items.size
    }
}
