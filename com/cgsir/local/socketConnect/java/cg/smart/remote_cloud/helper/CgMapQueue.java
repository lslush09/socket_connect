package cg.smart.remote_cloud.helper;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * author: shun
 * created on: 2017/11/9 19:16
 * description:
 */
public class CgMapQueue<K,V> {
    private Queue<CgEntry<K,V>> task_ids = new ConcurrentLinkedQueue<>();


    public synchronized void put(K k,V v,boolean replace){
        CgEntry<K,V> entry;
        if(!replace || (entry = getEntry(k))  == null){
            task_ids.add(new CgEntry(k,v));
        }else{
            entry.v = v;
        }
    }

    public void put(K k,V v){
        put(k,v,true);
    }

    public int size(){
        return task_ids.size();
    }

    public boolean isEmpty(){
        return task_ids.isEmpty();
    }

    public boolean containsKey(K k){
        return getEntry(k) != null;
    }

    public synchronized boolean remove(K k){
        Iterator<CgEntry<K,V>> iterator = task_ids.iterator();
        while (iterator.hasNext()){
            CgEntry<K,V> entry = iterator.next();
            if(entry.k.equals(k)){
              iterator.remove();
              return true;
            }
        }
        return false;
    }

    public synchronized V getValue(K k){
        CgEntry<K,V> entry = getEntry(k);
        return entry != null ? entry.v : null;
    }


    private CgEntry<K,V> getEntry(K k){
        Iterator<CgEntry<K,V>> iterator = task_ids.iterator();
        while (iterator.hasNext()){
            CgEntry<K,V> entry = iterator.next();
            if(entry.k.equals(k)){
                return entry;
            }
        }
        return null;
    }

    public synchronized CgEntry<K,V> poll(){
        if(!isEmpty()){
           return task_ids.poll();
        }
        return null;
    }

    public static class CgEntry<K,V>{
       final K k;
        V v;

        public CgEntry(K k, V v) {
            this.k = k;
            this.v = v;
        }

        public K getK() {
            return k;
        }

        public V getV() {
            return v;
        }

    }


}
