import json
import os
import re

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
PRICES_PATH = os.path.join(ROOT, "data", "prices_tr.json")
SHARED_PRICES_PATH = os.path.join(ROOT, "shared", "src", "commonMain", "composeResources", "files", "prices_tr.json")

# Import the strict validator from update_prices
from update_prices import is_strict_variant_compatible

with open(PRICES_PATH, "r", encoding="utf-8") as f:
    prices = json.load(f)

purged_offers = 0
for pid, pdata in prices.items():
    cat = pid.split("_")[0]
    cat_name = pid[len(cat)+1:].replace("_", " ")

    valid_offers = []
    for o in pdata.get("offers", []):
        url = o.get("url", "")
        # extract title from url slug
        slug = url.rstrip("/").split("/")[-1].replace("-", " ")
        if is_strict_variant_compatible(cat, slug, cat_name):
            valid_offers.append(o)
        else:
            purged_offers += 1
            print(f"Purged cross-match offer: [{cat}] '{cat_name}' != offer '{slug}'")

    pdata["offers"] = valid_offers
    if not valid_offers:
        pdata["currentPriceTry"] = None
        pdata["lowestPriceTry"] = None
        pdata["highestPriceTry"] = None
        pdata["priceHistory"] = []

print(f"Total purged false-match offers: {purged_offers}")

with open(PRICES_PATH, "w", encoding="utf-8") as f:
    json.dump(prices, f, indent=2, ensure_ascii=False)

with open(SHARED_PRICES_PATH, "w", encoding="utf-8") as f:
    json.dump(prices, f, indent=2, ensure_ascii=False)

print("Saved cleaned prices_tr.json files.")
