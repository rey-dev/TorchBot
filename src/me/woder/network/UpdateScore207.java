package me.woder.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import me.woder.bot.Client;

public class UpdateScore207 extends Packet{ 
    
     public UpdateScore207(Client c, DataInputStream in, DataOutputStream out) {
        super(c, in, out);
    }

    @Override
     public void read(Client c, int len){
          c.chat.readString();
          try {
            byte mode = c.in.readByte();
            System.out.println("Mode: " + mode);
            if(mode == 0){
                c.chat.readString();           
                c.in.readInt();
            }
          } catch (IOException e) {
            e.printStackTrace();
          }      
     }

}
