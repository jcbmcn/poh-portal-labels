import * as c2 from "@abextm/cache2";
import * as context from "viewer/context";

const cache = context.cache;
const maxObjId = 900000;

const results = [];

for (let id = 0; id < maxObjId; id++) {
    let item;
    try {
        item = await c2.Obj.load(cache, id);
    } catch {
        continue;
    }

    if (!item) continue;

    const name = item.name?.toLowerCase() || "";
    if (!name.includes("portal") || name.includes("portal frame") || name.includes("portal nexus")) continue;

    const width = item.width ?? item.resizeX ?? item.sizeX ?? null;
    if (width !== 2) continue;

    const actions = item.actions || [];
    const hasRemoveAction = actions.some(
        a => typeof a === "string" && a.toLowerCase().includes("remove")
    );

    if (!hasRemoveAction) continue;

    results.push({
        id,
        name: item.name || "(no name)",
        width,
        actions,
    });
}

// Sort by name (case-insensitive)
results.sort((a, b) => a.name.toLowerCase().localeCompare(b.name.toLowerCase()));

// Output
for (const obj of results) {
   // console.log(`Obj ID ${obj.id} (${obj.name}) → width: ${obj.width}, actions: ${JSON.stringify(obj.actions)}`);
		console.log(`${obj.id}, ${obj.name}`);
}
