package com.jinke.calligraphy.app.branch;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;

public class WorkQueue {

	private final static int nThreads = 3;
	private  static int QUEUE_SIZE = 300;
	private static int IMAGE_QUEUE_SIZE = 2;
	private final Thread[] threads;
	private final LinkedList queue;
	private final LinkedList imageQueue;
	
	private static WorkQueue workQueue = null;
	private boolean flipping = true;
	private boolean empty = true;
	
	private long start;
	private long end;
	
//	private List<String> workList = new ArrayList<String>();
//	
//	public boolean contains(String iden){
//		return workList.contains(iden);
//	}
//	public void addIdentify(String iden){
//		workList.add(iden);
//	}
//	public void removeIdentify(String iden){
//		workList.remove(iden);
//	}
	public void resetQueueSize(int newSize){
		QUEUE_SIZE = newSize;
	}
	
	private WorkQueue(int nThreads){
		
		this.queue = new LinkedList();
		this.imageQueue = new LinkedList();
		threads = new Thread[nThreads];
		for(int i=0;i<nThreads;i++){
			switch (i) {
			case 0:
			case 1:
				threads[i] = new PoolWorker();
				threads[i].start();
				break;
			case 2:
				threads[i] = new ImagePoolWorker();
				threads[i].start();
				break;
			default:
				break;
			}
			
		}
	}
	
	public static WorkQueue getInstance(){
		if(workQueue == null)
			workQueue = new WorkQueue(nThreads);
		
		return workQueue;
	}
	
	public boolean execute(Runnable r){
		synchronized (queue) {
			if(queue.contains(r)){
//				Log.v("workqueueadd", "contains r----------------" + queue.size());
				return false;
			}else{
				if(queue.size() > QUEUE_SIZE){
					queue.removeFirst();
				}
				queue.addLast(r);
//				Log.v("workqueueadd", "not contains r--------------" + queue.size());
				return true;
			}
				
		}
	}
	public boolean executeImage(Runnable r){
		synchronized (imageQueue) {
			if(imageQueue.contains(r)){
//				Log.v("workqueueadd", "contains r----------------" + imageQueue.size());
				return false;
			}else{
				if(imageQueue.size() >IMAGE_QUEUE_SIZE){
					imageQueue.removeFirst();
				}
				imageQueue.addLast(r);
//				Log.v("workqueueadd", "not contains r--------------" + imageQueue.size());
				return true;
			}
				
		}
	}

	public boolean removeWork(Runnable r){
		synchronized (imageQueue) {
			if(imageQueue.remove(r)){
//				Log.v("workqueueremove", "remove r------------------" + imageQueue.size());
				return true;
			}
//			else
//				Log.v("workqueueremove", "remove r  not contains------------------" + imageQueue.size());
			return false;
//			queue.notify();
		}
	}
	
	private	class PoolWorker extends Thread{
		Runnable r;
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(true){
				synchronized (queue) {
					while(queue.isEmpty()){
						try {
//							Log.e("queue", "wait");
							end = System.currentTimeMillis();
//							Log.e("workqueuetime", "-------------------------" +
//									"--------run finish use time:" + (end - start));
							queue.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}//end while
					r = (Runnable)queue.removeFirst();
					if(queue.isEmpty()){
//						Log.e("time", "queue empty begin update");
//						Start.c.flipHandler.sendEmptyMessage(0);
//						Log.v("flipupdate", "queue finish");
					}
				}
				try {
					r.run();
				} catch (RuntimeException e) {
					// TODO: handle exception
				}
				
			}
		}
		
	}
	private	class ImagePoolWorker extends Thread{
		Runnable r;
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(true){
				synchronized (imageQueue) {
					while(imageQueue.isEmpty()){
						try {
							Log.e("imagequeue", "wait");
							end = System.currentTimeMillis();
							Log.e("workqueuetime", "-------------------------" +
									"--------run finish use time:" + (end - start));
							imageQueue.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}//end while
					r = (Runnable)imageQueue.removeFirst();
					if(imageQueue.isEmpty()){
//						Log.e("time", "queue empty begin update");
//						Start.c.flipHandler.sendEmptyMessage(0);
//						Log.v("flipupdate", "queue finish");
					}
				}
				try {
					r.run();
				} catch (RuntimeException e) {
					// TODO: handle exception
				}
				
			}
		}
		
	}
	
	public void startFlipping(){
		flipping = true;
	}
	public void endFlipping(){
		
		synchronized (queue) {
			start = System.currentTimeMillis();
			Log.e("workqueuetime", "queue size:" + queue.size() + " start at:" + start);
			if(queue!= null && queue.size() != 0)
				queue.notifyAll();
		}
		synchronized (imageQueue) {
			if(imageQueue != null && imageQueue.size() != 0)
				imageQueue.notifyAll();
		}
	}
	
	
}
