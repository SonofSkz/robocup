/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robocup;

/**
 *
 * @author Mustafa
 * ADT Pair; stores two objects of any type
 */
public class Pair<A, B> {
    private A a;
    private B b;
    
    public Pair(){
        a = null;
        b = null;
    }
    
    public void setLeft(A a){
        this.a = a;
    }
    
    public void setRight(B b){
        this.b = b;
    }
    
    public A getLeft(){
        return a;
    }
    
    public B getRight(){
        return b;
    }
}
