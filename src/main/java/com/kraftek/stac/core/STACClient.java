package com.kraftek.stac.core;

import com.kraftek.stac.core.model.*;
import org.geotools.http.HTTPResponse;
import com.kraftek.stac.core.parser.STACParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Client for a STAC web service.
 * It allows for:
 * - browsing various STAC items (catalog, collections, items)
 * - searching collections
 * - downloading individual assets or full items
 *
 * @author Cosmin Cara
 */
public class STACClient {
    private final URL stacURL;
    final HttpClient client;

    /**
     * Initializes a new client for the given URL, with specific authentication instructions.
     * @param baseURL           The URL of the STAC web service
     * @param authentication    The authentication scheme
     */
    public STACClient(String baseURL, Authentication authentication) throws MalformedURLException {
        this.stacURL = new URL(baseURL);
        this.client= new HttpClient(authentication);
    }

    /**
     * Retrieves the catalog description from the remote STAC service
     */
    public Catalog getCatalog() throws IOException {
        final HTTPResponse response = this.client.get(this.stacURL);
        try (InputStream inStream = response.getResponseStream()) {
            return new STACParser().parseCatalogResponse(inStream);
        }
    }
    /**
     * Retrieves the list of collection descriptions from the remote STAC service
     */
    public CollectionList listCollections() throws IOException {
        final HTTPResponse response = this.client.get(new URL(this.stacURL + "/collections"));
        try (InputStream inStream = response.getResponseStream()) {
            return new STACParser().parseCollectionsResponse(inStream);
        }
    }
    /**
     * Retrieves a single collection description from the remote STAC service
     * @param collectionName The name of the collection
     */
    public Collection getCollection(String collectionName) throws IOException {
        final HTTPResponse response = this.client.get(new URL(this.stacURL + "/collections/" + collectionName));
        try (InputStream inStream = response.getResponseStream()) {
            return new STACParser().parseCollectionResponse(inStream);
        }
    }
    /**
     * Retrieves the list of items (only the first page) from a given collection
     * @param collectionName The name of the collection
     */
    public ItemCollection listItems(String collectionName) throws IOException {
        return listItems(collectionName, 0, 0);
    }
    /**
     * Retrieves a page of items from a given collection.
     * @param collectionName The name of the colleciton
     * @param pageNumber The page number (1-based)
     * @param pageSize The page size
     */
    public ItemCollection listItems(String collectionName, int pageNumber, int pageSize) throws IOException {
        String href = this.stacURL + "/collections/" + collectionName + "/items";
        if (pageNumber > 0 & pageSize > 0) {
            href += "?page=" + pageSize + "&limit=" + pageSize;
        }
        final HTTPResponse response = this.client.get(new URL(href));
        try (InputStream inStream = response.getResponseStream()) {
            return new STACParser().parseItemCollectionResponse(inStream);
        }
    }
    /**
     * Retrieves a single item from a collection.
     * @param collectionName    The collection name
     * @param itemId            The item identifier
     */
    public Item getItem(String collectionName, String itemId) throws IOException {
        String href = this.stacURL + "/collections/" + collectionName + "/items/" + itemId;
        final HTTPResponse response = this.client.get(new URL(href));
        try (InputStream inStream = response.getResponseStream()) {
            return new STACParser().parseItemResponse(inStream);
        }
    }

    /**
     * Returns the first page of items that match the given parameters from a collection
     * @param collectionName    The collection name
     * @param parameters        The search criteria
     */
    public ItemCollection search(String collectionName, Map<String, Object> parameters) throws IOException {
        return search(collectionName, parameters, 0, 0);
    }
    /**
     * Returns a page of items that match the given parameters from a collection
     * @param collectionName    The collection name
     * @param parameters        The search criteria
     * @param pageNumber        The page number (1-based)
     * @param pageSize          The page size
     */
    public ItemCollection search(String collectionName, Map<String, Object> parameters, int pageNumber, int pageSize) throws IOException {
        if (parameters == null) {
            return listItems(collectionName);
        }
        final Catalog catalog = getCatalog();
        Link searchLink = catalog.getLinks().stream().filter(l -> "search".equals(l.getRel())).findFirst().orElse(null);
        if (searchLink == null) {
            throw new IOException("Search not supported on catalog " + catalog.getId());
        }
        StringBuilder href = new StringBuilder(searchLink.getHref() + "?");
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            href.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        href.append("collections=").append(collectionName).append("&");
        if (pageNumber > 0 & pageSize > 0) {
            href.append("page=").append(pageSize).append("&limit=").append(pageSize);
        }
        if (href.charAt(href.length() - 1) == '&') {
            href.setLength(href.length() - 1);
        }
        final HTTPResponse response = this.client.get(new URL(href.toString()));
        try (InputStream inStream = response.getResponseStream()) {
            return new STACParser().parseItemCollectionResponse(inStream);
        }
    }

    /**
     * Downloads an entire item (all downloadable assets of the item)
     * @param item      The item
     * @param folder    The destination folder
     */
    public void download(Item item, Path folder) throws IOException {
        final Map<String, Asset> assets = item.getAssets();
        if (assets != null && assets.size() > 0) {
            final Path target = folder.resolve(item.getId());
            Files.createDirectories(target);
            for (Asset asset : assets.values()) {
                download(asset, target);
            }
        }
    }
    /**
     * Downloads an individual asset from an item.
     * @param asset     The asset
     * @param folder    The destination folder
     */
    public void download(Asset asset, Path folder) throws IOException {
        final String href = asset.getHref();
        final String name = href.substring(href.lastIndexOf('/') + 1);
        final HTTPResponse response = this.client.get(new URL(href));
        try (ReadableByteChannel inputChannel = Channels.newChannel(response.getResponseStream());
             WritableByteChannel outputChannel = Channels.newChannel(Files.newOutputStream(folder.resolve(name)))) {
            final ByteBuffer buffer = ByteBuffer.allocateDirect(65536);
            while (inputChannel.read(buffer) != -1) {
                buffer.flip();
                outputChannel.write(buffer);
                buffer.compact();
            }
            buffer.flip();
            while (buffer.hasRemaining()) {
                outputChannel.write(buffer);
            }
        }
    }
    /**
     * Downloads an individual asset given as a URL from an item.
     * @param href     The URL of the asset
     * @param folder   The destination folder
     */
    public void download(String href, Path folder) throws IOException {
        final String name = href.substring(href.lastIndexOf('/') + 1);
        final HTTPResponse response = this.client.get(new URL(href));
        try (ReadableByteChannel inputChannel = Channels.newChannel(response.getResponseStream());
             WritableByteChannel outputChannel = Channels.newChannel(Files.newOutputStream(folder.resolve(name)))) {
            final ByteBuffer buffer = ByteBuffer.allocateDirect(65536);
            while (inputChannel.read(buffer) != -1) {
                buffer.flip();
                outputChannel.write(buffer);
                buffer.compact();
            }
            buffer.flip();
            while (buffer.hasRemaining()) {
                outputChannel.write(buffer);
            }
        }
    }
    /**
     * Returns an input stream for an asset
     * @param asset     The asset
     */
    public InputStream download(Asset asset) throws IOException {
        final HTTPResponse response = this.client.get(new URL(asset.getHref()));
        return response.getResponseStream();
    }
    /**
     * Returns an input stream for an asset given by its URL
     * @param href     The URL of the asset
     */
    public InputStream download(String href) throws IOException {
        final HTTPResponse response = this.client.get(new URL(href));
        return response.getResponseStream();
    }

}
