# Gojo Spells (Iron's Spells 'n Spellbooks addon, Forge 1.20.1)

Adds four spells to Iron's Spells 'n Spellbooks:

| Spell | School | Cast | What it does |
| --- | --- | --- | --- |
| Blue | Ender | Instant | 3 s attraction point; pulls entities in and damages those near the centre |
| Red | Evocation | Instant | Repulsive blast at the aim point: damage plus strong knockback |
| Hollow Purple | Ender | 2 s charge | Beam that damages everything in line and carves a tunnel through terrain |
| Domain Expansion: Infinite Void | Ender | 3 s | 16-block sphere for 10 s; everyone inside except you is slowed, blinded, nauseous, weakened and immobilised |

## Get a jar without installing anything (GitHub does the build)

1. Make a free account at github.com and click **New repository** (any name, Public).
2. On the new repo page click **uploading an existing file**, then drag in **everything inside this folder**
   (including the hidden `.github` folder, the `src` folder and all the `.gradle` files). Click **Commit changes**.
3. Open the **Actions** tab. The "Build mod" run starts by itself (about 3-5 minutes). If it does not, click
   "Build mod" -> **Run workflow**.
4. When it shows a green tick, open the run and download **gojo_spells-jar** at the bottom. Unzip it to get the `.jar`.
5. Put the `.jar` in your `mods` folder next to **Forge 1.20.1** and **Iron's Spells 'n Spellbooks** (plus its required libraries).

If the run shows a red cross, open it, copy the error text and send it to Claude.

## Using the spells in game

Open the Creative inventory and look in the Iron's Spells tab for the scrolls: Blue, Red, Hollow Purple and Domain Expansion: Infinite Void
will appear with the other spell scrolls. Right-click a scroll to learn or cast it, or put it in a spellbook.

## Tweaks

In `GojoEffects.java`:

- `PURPLE_BREAKS_BLOCKS` - set to `false` to stop Hollow Purple destroying terrain.
- `DOMAIN_FREEZES_PLAYERS` - set to `true` to also freeze other players in the domain.

Mana cost, cast time and cooldown are at the top of each file in `spells/`.
