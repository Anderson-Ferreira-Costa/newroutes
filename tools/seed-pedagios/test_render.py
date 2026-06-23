#!/usr/bin/env python3
"""Quick test of render_portlet endpoint."""
import requests, re

url = (
    "https://portal.antt.gov.br/c/portal/render_portlet"
    "?p_l_id=1668"
    "&p_p_id=com_liferay_asset_publisher_web_portlet_AssetPublisherPortlet_INSTANCE_m2By5inRuGGs"
    "&p_p_lifecycle=0"
    "&p_p_state=normal"
    "&p_p_mode=view"
    "&_com_liferay_asset_publisher_web_portlet_AssetPublisherPortlet_INSTANCE_m2By5inRuGGs_struts_action=%2Fasset_publisher%2Fsearch"
    "&_com_liferay_asset_publisher_web_portlet_AssetPublisherPortlet_INSTANCE_m2By5inRuGGs_type=content"
    "&_com_liferay_asset_publisher_web_portlet_AssetPublisherPortlet_INSTANCE_m2By5inRuGGs_keywords=tarifas+pedagio+fernao+dias"
    "&_com_liferay_asset_publisher_web_portlet_AssetPublisherPortlet_INSTANCE_m2By5inRuGGs_cur=0"
)
headers = {"User-Agent": "NewRoutes/1.0"}
resp = requests.get(url, headers=headers, timeout=15)
print(f"Status: {resp.status_code}")
print(f"URL: {resp.url}")

ids = re.findall(r'/content/id/(\d+)', resp.text)
print(f"content/id/ IDs: {ids}")

ae_ids = re.findall(r'assetEntryId["\'=]+(\d+)', resp.text)
print(f"assetEntryIds: {ae_ids[:15]}")

tarifa_count = resp.text.lower().count('tarifa')
print(f"tarifa occurrences: {tarifa_count}")

content_links = re.findall(r'href=["\x27]([^"\x27]*content/[^"\x27]*)', resp.text)
for l in content_links[:15]:
    print(f"  {l}")

# Check portlet boundaries
portlets = re.findall(r'portlet-boundary[^"\']+["\x27]', resp.text)
print("\nPortlets:")
for p in sorted(set(portlets)):
    print(f"  {p[:150]}")

# Also check for any text content with tarifas
text_sections = re.findall(r'>([^<]{10,200}tarifa[^<]{0,100})<', resp.text, re.I)
print(f"\nTarifa text sections: {len(text_sections)}")
for t in text_sections[:5]:
    print(f"  {t}")
