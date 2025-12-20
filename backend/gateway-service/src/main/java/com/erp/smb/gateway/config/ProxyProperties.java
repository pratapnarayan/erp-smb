package com.erp.smb.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "proxy")
public class ProxyProperties {
  private List<Route> routes = new ArrayList<>();

  public List<Route> getRoutes() {
    return routes;
  }

  public void setRoutes(List<Route> routes) {
    this.routes = routes;
  }

  public static class Route {
    private String id;
    private String path;
    private String url;
    private String uri;
    private Boolean stripPrefix; // optional per-route behavior

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getPath() {
      return path;
    }

    public void setPath(String path) {
      this.path = path;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getUri() {
      return uri;
    }

    public void setUri(String uri) {
      this.uri = uri;
    }

    public Boolean getStripPrefix() {
      return stripPrefix;
    }

    public void setStripPrefix(Boolean stripPrefix) {
      this.stripPrefix = stripPrefix;
    }
  }
}
