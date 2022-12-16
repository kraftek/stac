# stac
Simple STAC client for Java.
The client allows for:
* browsing various STAC items (catalog, collections, items)
* searching collections
* downloading individual assets or full items

It supports basic and token authentication.

The client was initially implemented in the [TAO](https://github.com/tao-org) framework. This is a migration from that repository with no dependencies on the TAO framework modules.

Example of usage:
```
STACClient client = new STACClient("https://earth-search.aws.element84.com/v1", null);
CollectionList list = client.listCollections();
String collectionName;
for (Collection collection : list.getCollections()) {
  collectionName = collection.getId();
  break;
}
Map<String, Object> params = new HashMap<>();
params.put("bbox", "20.2201924985,43.6884447292,29.62654341,48.2208812526");
params.put("datetime", "2022-05-01T00:00:00Z/2022-05-02T23:59:59Z");
ItemCollection results = client.search(collectionName, params);
List<Item> features = results.getFeatures();
Path downloadFolder = Paths.get("/tmp");
for (Item item : features) {
  // do something with the item
  Map<String, Asset> assets = item.getAssets();
  for (Map.Entry<String, Asset> asset : assets.entrySet()) {
    // download the asset if it has a HTTP(s) href
    if (asset.getHref() != null && asset.getHref().startsWith("http")) {
      client.download(asset, downloadFolder);
    }
  }
  // download all the item assets that have HTTP(s) hrefs
  item.download(item, downloadFolder);
}
```
