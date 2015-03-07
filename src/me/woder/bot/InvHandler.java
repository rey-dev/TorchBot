package me.woder.bot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.woder.network.Packet;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class InvHandler {
    Client c;
    public List<Slot> inventory = new ArrayList<Slot>();
    public int currentSlot;
    public Short actionNumber;
    
    public InvHandler(Client c){
        this.c = c;
        currentSlot = 36;
        actionNumber = 1;
        //this.selectSlot((short)0);
    }
    
    public List<Slot> getInventory() {
    	return inventory;
    }
    
    public int getCurrentSlot() {
    	return currentSlot;
    }
    
    public void creativeSetSlot(short id, Slot slot){
        ByteArrayDataOutput buf = ByteStreams.newDataOutput();    
        try {
         Packet.writeVarInt(buf, 16);
         buf.writeShort(id);
         slot.sendSlot(buf);
         c.net.sendPacket(buf, c.out);
        } catch (IOException e) {
         e.printStackTrace();
        }
    }
    
    //pre: id is from 0-8
    //post: sets the active hotkey slot to the given id, and stores the current slot as an inventory location to currentSlot.
    public void selectSlot(short id){
        ByteArrayDataOutput buf = ByteStreams.newDataOutput();    
        try {
         currentSlot = (int)id+36;
         Packet.writeVarInt(buf, 9);
         buf.writeShort(id);
         c.net.sendPacket(buf, c.out);
        } catch (IOException e) {
         e.printStackTrace();
        }
    }
    
    public void setSlot(Slot slot){
        boolean found = false;
        for(Slot s : inventory){
            if(s.slotnum == slot.slotnum){
                found = true;
                s.setSlot(slot);
            }
        }
        if(!found){
           inventory.add(slot);
        }
    }
    
    public Slot getItem(short id) {
    	for(Slot s: inventory) {
    		if (s.getId()==id) {
    			return s;
    		}
    	}
    	return null;
    }
    
    public boolean swapTo(short id) {
    	Slot item = getItem(id);
    	if (item!=null) {
    		swapSlots(currentSlot, item.getNum());
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public void swapSlots(final int a, final int b) {
    	if(inventory.get(a)==null||inventory.get(b)==null) {
    		System.out.println("Passed slots do not exist");
    		return;
    	}
    	//c.chat.sendMessage("swapping slots "+a+" and "+b);
    	int delay = 100;
    	final Timer timer = new Timer();
    	Slot originalA = c.invhandle.inventory.get(a);
    	Slot originalB = c.invhandle.inventory.get(b);
    	final Slot emptyA = new Slot(originalA.getNum(),(short)-1,(byte)0,(short)0,(byte)0);//the location of A, with no contents
    	final Slot slotB = new Slot(originalB.getNum(),originalA.getId(),
    			originalA.getCount(),originalA.getDamage(),originalA.getNbtlen());//the location of B, with the contents of A
    	final Slot slotA = new Slot(originalA.getNum(),originalB.getId(),
    			originalB.getCount(),originalB.getDamage(),originalB.getNbtlen());//the location of A, with the contents of B
    	
    	timer.schedule(new TimerTask(){
            public void run() {
            	clickSlot(a);//pick up contents of a
            	setSlot(emptyA);
            }
        }, delay);
    	
    	timer.schedule(new TimerTask(){
            public void run() {
            	clickSlot(b);//pick up B's contents ,set down A's contents in B's location
            	setSlot(slotB);
            	            	
            }
        }, delay*2);
    	
    	timer.schedule(new TimerTask(){
            public void run() {
            	clickSlot(a);//set down B's contents in A's original location
            	setSlot(slotA);
            	
            }
        }, delay*3);
    	
    	timer.schedule(new TimerTask(){
            public void run() {
            	closeInventory(); 
            	timer.cancel();
            }
        }, delay*4);
    	//pray to Notch that we don't need to handle or send confirmations
    }
    
    public void clickSlot(int num) {
    	ByteArrayDataOutput buf = ByteStreams.newDataOutput();   
    	Slot s = inventory.get(num);
        try {
         //write the clicking info
         Packet.writeVarInt(buf, 14);
         buf.writeByte(0);
         buf.writeShort(s.getNum());
         buf.writeByte(0);
         buf.writeShort(actionNumber);
         actionNumber++;
         buf.writeByte(0);
         //write the slot structure we have
         s.sendSlot(buf);
         c.net.sendPacket(buf, c.out);
        } catch (IOException e) {
         e.printStackTrace();
        }
    }
    
    public void closeInventory() {
        ByteArrayDataOutput buf = ByteStreams.newDataOutput();    
        try {
         Packet.writeVarInt(buf, 13);
         buf.writeByte(0);
         c.net.sendPacket(buf, c.out);
        } catch (IOException e) {
         e.printStackTrace();
        }
    }
    
    

}
