package com.github.cao.awa.annuus.map.expire;

/**
 * A listener for expired object events.
 * 
 * @param <K> Key type
 * @param <V> Value type
 */
public interface ExpirationListener<K, V> {
  /**
   * Called when a map entry expires.
   * 
   * @param key Expired key
   * @param value Expired value
   */
  void onExpired(K key, V value);
}