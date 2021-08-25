import kotlinx.browser.*;
import org.w3c.dom.*;
import org.w3c.dom.events.*;
import kotlin.collections.*;

val body : HTMLBodyElement=document.body as HTMLBodyElement;
val wall_size=16.0;
val size_of_dot=8.0;
val wall_distance_from_top=96.0;

/*flip the bits of a UByte*/
/*11010001 -> 10001011*/
fun flipbits(b:UByte):UByte{
    var bcontain : UInt=b.toUInt();
    var result:UInt=0u;
    repeat(8){
        result*=2u
        if (bcontain and 1u==1u ){
            result+=1u

        }
        bcontain/=2u
    }
    return result.toUByte()




}
/*flip an entire array of ubytes*/
fun flip(ua: UByteArray):UByteArray{
    var result= UByteArray(16)
    var cnt=0;
    for (b in ua) {
        result.set(cnt,flipbits(b))
        cnt++;
    }
    return result;


}
val LEFT=0
val RIGHT=1
/*
The graphics are stored in a 1bpp
UByte Array. All the graphics (except the bullets)
are exactly 8 dots wide. so each line is exactly 1 byte.
This is also kinda how it works on an original Atari 2600.
except only 1 line can be in "VRAM" at a time.
 */
val sherrif_left_image_data : UByteArray = ubyteArrayOf(
    0b00011000u,
    0b00111100u,
    0b00011000u,
    0b00011000u,
    0b00111100u,
    0b00111100u,
    0b01011010u,
    0b10011001u,
    0b00011000u,
    0b00011000u,
    0b00011000u,
    0b00101000u,
    0b00101000u,
    0b00101000u,
    0b01000100u,
    0b01000100u
)
val sherrif_left_walk_image_data : UByteArray = ubyteArrayOf(
    0b00011000u,
    0b00111100u,
    0b00011000u,
    0b00011000u,
    0b00111100u,
    0b00111100u,
    0b01011010u,
    0b10011001u,
    0b00011000u,
    0b00011000u,
    0b00011000u,
    0b00101000u,
    0b00101000u,
    0b00010000u,
    0b00101000u,
    0b01000100u
)

val obstacle_image_data : UByteArray = ubyteArrayOf(
    0b00001001u,
    0b00001010u,
    0b10001010u,
    0b01001100u,
    0b00101100u,
    0b00011000u,
    0b00001001u,
    0b00001110u,
    0b00001000u,
    0b00001000u,
    0b10001000u,
    0b01001000u,
    0b00101000u,
    0b00011000u,
    0b00011000u,
    0b00011000u
)

val sherrif_left_shoot_down_image_data = ubyteArrayOf(
    0b00011000u,
    0b00111100u,
    0b00011000u,
    0b00011000u,
    0b00010000u,
    0b00111000u,
    0b00011110u,
    0b00011010u,
    0b00011001u,
    0b00011000u,
    0b00011000u,
    0b00011000u,
    0b00011000u,
    0b00011000u,
    0b00000100u,
    0b01111110u
)
val sherrif_left_shoot_straight_image_data = ubyteArrayOf(
    0b00011000u,
    0b00111100u,
    0b00011000u,
    0b00011000u,
    0b00010011u,
    0b00111110u,
    0b00011000u,
    0b00011000u,
    0b00011000u,
    0b00011000u,
    0b00011000u,
    0b00011000u,
    0b00011000u,
    0b00011000u,
    0b00000100u,
    0b01111110u
)
val sherrif_left_shoot_up_image_data = ubyteArrayOf(
    0b00011000u,
    0b00111100u,
    0b00011001u,
    0b00011010u,
    0b00010110u,
    0b00111000u,
    0b00011000u,
    0b00011000u,
    0b00011000u,
    0b00011000u,
    0b00011000u,
    0b00011000u,
    0b00011000u,
    0b00011000u,
    0b00000100u,
    0b01111110u
)
val sherrif_left_hit = ubyteArrayOf(
    0b00000000u,
    0b01100000u,
    0b11110000u,
    0b11110000u,
    0b01100000u,
    0b01100000u,
    0b01100000u,
    0b01100000u,
    0b11100000u,
    0b11100000u,
    0b11100000u,
    0b11100000u,
    0b11100000u,
    0b10000000u,
    0b10000000u,
    0b11111111u
)
/*Image data for both sides*/
val sherrif_image_data=arrayOf(sherrif_left_image_data,flip(sherrif_left_image_data));
val sherrif_walk_image_data=arrayOf(sherrif_left_walk_image_data,flip (sherrif_left_walk_image_data));
val sherrif_shoot_straight_image_data=arrayOf(sherrif_left_shoot_straight_image_data,flip(sherrif_left_shoot_straight_image_data))
val sherrif_shoot_down_image_data=arrayOf(sherrif_left_shoot_down_image_data,flip(sherrif_left_shoot_down_image_data))
val sherrif_shoot_up_image_data=arrayOf(sherrif_left_shoot_up_image_data,flip(sherrif_left_shoot_up_image_data))
val sherrif_hit = arrayOf(sherrif_left_hit,flip(sherrif_left_hit))

var world_active=true //This variable pauses all intevals except the draw screen one.

/*This is a class for the bush in the middle of the field*/
class Obstacle {
    private val color:String = "#889933";
    val x : Double=400.0-32.0;
    val y : Double=300.0+(wall_distance_from_top/2)-64.0;
    val w = 8;
    val h = 16;
    fun truewidth():Double{return w*size_of_dot;}
    fun trueheight():Double{return h*size_of_dot;}
    fun drawOn(ctx : CanvasRenderingContext2D){
        ctx.fillStyle=color;
        /* index the 1 byte lines*/
        for (i in 0..h){
            var b:UByte = obstacle_image_data[i];
            /* loop grabs bits starting from the most significant and draws them on screen*/
            for (j in 0..w){
                if (b>=0x80u){
                    ctx.fillRect(x + j * size_of_dot, y + i * size_of_dot, size_of_dot, size_of_dot)
                }
                b=(b*2u).toUByte()

            }
        }
    }
}
val obstacle=Obstacle();

/*This class is in charge of the bullet that is shot from the gun of the players*/
class Bullet{
    private var x:Double;
    private var y:Double;
    private val color:String;
    private var deltax:Double;
    private var deltay:Double;
    private var this_interval=-200;
    private val side : Int;
    val speed=2.0;
    constructor(c : String, side:Int) {
        color=c;
        x=-200.0
        y=-200.0
        this.side=side;
        this.deltax=0.0;
        this.deltay=0.0;
    }
    /*moves the bullet to the right place and starts the movement interval*/
    fun shoot(x:Double,y:Double,deltax:Double,deltay: Double){
        this.x=x
        this.y=y
        this.deltax=deltax*speed;
        this.deltay=deltay*speed;
        this_interval=window.setInterval(this::move,10)
    }
    fun isInScreen():Boolean{
        if ((x>800.0+size_of_dot) or (y>600.0+size_of_dot) or (x<-size_of_dot) or (y<-size_of_dot)) {
            window.clearInterval(this_interval)

            return false;
        }

        return true;
    }
    /*this is the function to make an interval from*/
    fun move(){
        if (!world_active) {return}
        x+=deltax;
        y+=deltay;
        /*reflect the bullet if it hits one of the walls*/
        if ((this.y<=wall_size+wall_distance_from_top) or (this.y>=600-wall_size)){
            deltay=-deltay
        }
        /* too lazy to write a better collision handler */
        var hit=false;
        /*check to see if the bullet hit the obstacle*/
        if ((this.x + size_of_dot > obstacle.x) and (this.x + size_of_dot< obstacle.x + size_of_dot) and (this.y < obstacle.y + obstacle.trueheight()) and (this.y + size_of_dot > obstacle.y)) {
            hit=true
        }
        if ((this.x > obstacle.x + obstacle.truewidth() - size_of_dot) and (this.x < obstacle.x + obstacle.truewidth()) and (this.y < obstacle.y + obstacle.trueheight()) and (this.y + size_of_dot > obstacle.y)) {
            hit=true
        }
        if ((this.y + size_of_dot > obstacle.y) and (this.y + size_of_dot < obstacle.y + size_of_dot) and (this.x < obstacle.x + obstacle.truewidth()) and (this.x + size_of_dot > obstacle.x)) {
            hit=true
        }
        if ((this.y > obstacle.y + obstacle.trueheight() - size_of_dot) and (this.y < obstacle.y + obstacle.trueheight()) and (this.x < obstacle.x + obstacle.truewidth()) and (this.x + size_of_dot > obstacle.x)) {
            hit=true
        }
        val opposing_sherrif = sherrifs[1-side]
        /*check to see if the bullet hit the enemy player*/
        if ((this.x + size_of_dot > opposing_sherrif.x) and (this.x + size_of_dot < opposing_sherrif.x + size_of_dot) and (this.y < opposing_sherrif.y + opposing_sherrif.trueheight()) and (this.y + size_of_dot > opposing_sherrif.y)) {
            hit=true
            opposing_sherrif.hit()

        }
        if ((this.x > opposing_sherrif.x + opposing_sherrif.truewidth() - size_of_dot) and (this.x < opposing_sherrif.x + opposing_sherrif.truewidth()) and (this.y < opposing_sherrif.y + opposing_sherrif.trueheight()) and (this.y + size_of_dot > opposing_sherrif.y)) {
            hit=true
            opposing_sherrif.hit()
        }
        if ((this.y + size_of_dot > opposing_sherrif.y) and (this.y + size_of_dot< opposing_sherrif.y + size_of_dot) and (this.x < opposing_sherrif.x + opposing_sherrif.truewidth()) and (this.x + size_of_dot> opposing_sherrif.x)) {
            hit=true
            opposing_sherrif.hit()
        }
        if ((this.y > opposing_sherrif.y + opposing_sherrif.trueheight() - size_of_dot) and (this.y < opposing_sherrif.y + opposing_sherrif.trueheight()) and (this.x < opposing_sherrif.x + opposing_sherrif.truewidth()) and (this.x + size_of_dot > opposing_sherrif.x)) {
            hit=true
            opposing_sherrif.hit()
        }
        /*move the bullet out of bounds if hit something*/
        if (hit){
            x=-200.0;
            y=-200.0;
            deltax=0.0;
            deltay=0.0;
            window.clearInterval(this_interval);
            this_interval=-200


        }

    }


    fun drawOn(ctx : CanvasRenderingContext2D){
        if (isInScreen()){
            ctx.fillRect(x , y, size_of_dot, size_of_dot)
        }
    }


}

var world_resumer : Int=-2; //this stores the interval to resume the world so it could be cleared by the win function. Making the world permanently paused
/*This is the class for the players*/
class Sherrif{
    val color:String;
    var x : Double;
    var y : Double;
    val w = 8;
    val h = 16;
    val step_time=20;
    private var ismoving : Int=0;
    private val bullet:Bullet;
    private var isaiming : Boolean=false;
    private var isshooting : Boolean=false;
    private var vertical_movement:Int=0;
    val speed = 3;
    private var curImageState : UByteArray;
    private val side :Int;
    private var hits=0;
    constructor(c : String, posx : Double, posy : Double, side:Int){
        color=c
        x=posx;y=posy
        this.side=side;
        curImageState=sherrif_image_data[side];
        bullet=Bullet(color,side)
    }
    fun drawOn(ctx : CanvasRenderingContext2D) {
        ctx.fillStyle = color;
        /* index the 1 byte lines*/
        for (i in 0..h) {
            var b: UByte = curImageState[i];
            /* loop grabs bits starting from the most significant and draws them on screen*/
            for (j in 0..w) {
                if (b >= 0x80u) {
                    ctx.fillRect(x + j * size_of_dot, y + i * size_of_dot, size_of_dot, size_of_dot)
                }
                b = (b * 2u).toUByte()

            }
        }
        if (this.isshooting){
            bullet.drawOn(ctx)
        }
    }
    /*This is called when a bullet hits the guy*/
    fun hit() {
        hits += 1;
        curImageState = sherrif_hit[side];
        world_active = false
        world_resumer = window.setTimeout({ world_active = true; this.curImageState=sherrif_image_data[this.side] }, 1000)
    }
    fun gethits():Int{return hits}
    fun truewidth():Double{return w*size_of_dot;}
    fun trueheight():Double{return h*size_of_dot;}

    /*This is called from the Interval*/
    fun move(xdelta:Double, ydelta: Double){
        if (!world_active) {return}
        if (this.isshooting){
            if(!bullet.isInScreen()){
                curImageState = sherrif_image_data[side];
                this.isshooting=false

            }
        }
        else if (!this.isaiming ) {
            /*Actually move the sherrif*/
            this.x += xdelta*speed; this.y += ydelta*speed;
            /*check to see if the sherrif is trying to move off screen and prevent it*/
            if (this.y < wall_size+wall_distance_from_top) {
                this.y = wall_size+wall_distance_from_top
            }
            if (this.y > 600.0 - wall_size - size_of_dot * this.h) {
                this.y = 600.0 - wall_size - size_of_dot * this.h;
            }
            if (this.x < this.side * 400.0) {
                this.x = this.side * 400.0;
            }
            if (this.x > (400.0 * (this.side + 1.0)) - size_of_dot* this.w) {
                this.x = (400.0 * (this.side + 1.0)) - size_of_dot * this.w;
            }
            /*check to see if the sherrif is trying to move into the obstacle.*/
            if ((this.x + this.truewidth() > obstacle.x) and (this.x + this.truewidth() < obstacle.x + size_of_dot) and (this.y < obstacle.y + obstacle.trueheight()) and (this.y + this.trueheight() > obstacle.y)) {
                this.x = obstacle.x - this.truewidth()
            }
            if ((this.x > obstacle.x + obstacle.truewidth() - size_of_dot) and (this.x < obstacle.x + obstacle.truewidth()) and (this.y < obstacle.y + obstacle.trueheight()) and (this.y + this.trueheight() > obstacle.y)) {
                this.x = obstacle.x + obstacle.truewidth()
            }
            if ((this.y + this.trueheight() > obstacle.y) and (this.y + this.trueheight() < obstacle.y + size_of_dot) and (this.x < obstacle.x + obstacle.truewidth()) and (this.x + this.truewidth() > obstacle.x)) {
                this.y = obstacle.y - this.trueheight()
            }
            if ((this.y > obstacle.y + obstacle.trueheight() - size_of_dot) and (this.y < obstacle.y + obstacle.trueheight()) and (this.x < obstacle.x + obstacle.truewidth()) and (this.x + this.truewidth() > obstacle.x)) {
                this.y = obstacle.y + obstacle.trueheight()
            }
        }


    }
    /*This swaps the neutral and step positions if the sherrif is moving*/
    fun step_cycle_handler(){
        if (!world_active) {return}
        if (this.isaiming or this.isshooting){


        } else if (ismoving>0) {
            if (curImageState == sherrif_image_data[side]) {
                curImageState = sherrif_walk_image_data[side];
            } else if (curImageState == sherrif_walk_image_data[side]) {
                curImageState = sherrif_image_data[side];
            }
        } else {
            curImageState = sherrif_image_data[side];

        }
    }

    /*This is called from the buttonDown function to start standing in place and aim the gun*/
    fun aim(){
        if (!this.isshooting) {
            this.isaiming = true;
            aim_fix()
        }

    }
    /* This just makes sure the image is one of the shoot states*/
    fun aim_fix(){
        if (this.isaiming) {
            if (vertical_movement > 0) {
                curImageState = sherrif_shoot_down_image_data[side];
            } else if (vertical_movement < 0) {
                curImageState = sherrif_shoot_up_image_data[side];
            } else {
                curImageState = sherrif_shoot_straight_image_data[side];

            }
        }
    }
    /*this is called from the keyUp functions and takes the corresponding out of bounds bullet and shoots it*/
    fun fire(){
        if (!this.isshooting) {
            this.isshooting = true
            this.isaiming = false;
            this.bullet.shoot(
                this.x + truewidth()*(1-this.side),
                this.y + trueheight() / 2-(-vertical_movement+1)*3*size_of_dot,
                ((1 - 2 * side)*3).toDouble(),
                (vertical_movement*3).toDouble()
            )
            aim_fix(); //make sure the image is still aim.
        }

    }
    /*This is called from keyup of one of the movement intervals and makes sure all the redundant data is alligned*/
    fun stop_one_move(keycode:String){
        /*clear the interval*/
        val interval=movementIntervals.get(keycode);
        if (interval!=null){
            window.clearInterval(interval)
        }


        val delta=getDelta(keycode)

        /*keep track of the vertical movement for aiming*/
        val y=delta.second
        if (y > 0) {
            vertical_movement-=1;
        } else if (y < 0 ) {

            vertical_movement -= -1;
        }

        /*make sure if you are aiming you aim the right way.*/
        this.aim_fix();
        /*subtract 1 from the amount of movement intervals*/
        this.ismoving-=1;
    }
    /*This creates the interval when keydown happens*/
    fun accelerate(xdelta :Double, ydelta:Double):Int{
        if (ydelta > 0) {
            vertical_movement+=1;
        } else if (ydelta < 0 ) {
            vertical_movement += -1;
        }
        this.ismoving+=1;
        return window.setInterval(::move,step_time,xdelta,ydelta)
    }


}

val sherrif1=Sherrif("#663399",200.0,200.0, LEFT)
val sherrif2=Sherrif("#339966",600.0,400.0,RIGHT)
val sherrifs = arrayOf(sherrif1,sherrif2)
val naught:UByteArray= ubyteArrayOf(
    0b01111110u,
    0b11111111u,
    0b11000011u,
    0b11000011u,
    0b11000011u,
    0b11000011u,
    0b11000011u,
    0b11000011u,
    0b11000011u,
    0b11000011u,
    0b11111111u,
    0b01111110u
)
val one:UByteArray= ubyteArrayOf(
    0b00111100u,
    0b00111100u,
    0b00001100u,
    0b00001100u,
    0b00001100u,
    0b00001100u,
    0b00001100u,
    0b00001100u,
    0b00001100u,
    0b00001100u,
    0b00001100u,
    0b00000000u,
)
val two:UByteArray= ubyteArrayOf(
    0b11111111u,
    0b11111111u,
    0b00000011u,
    0b00000011u,
    0b00000011u,
    0b11111111u,
    0b11111111u,
    0b11000000u,
    0b11000000u,
    0b11000000u,
    0b11111111u,
    0b11111111u,
)
val three:UByteArray= ubyteArrayOf(
    0b11111111u,
    0b11111111u,
    0b00000011u,
    0b00000011u,
    0b00000011u,
    0b11111111u,
    0b11111111u,
    0b00000011u,
    0b00000011u,
    0b00000011u,
    0b11111111u,
    0b11111111u,
)
val four:UByteArray= ubyteArrayOf(
    0b00011000u,
    0b00111000u,
    0b01111000u,
    0b11011000u,
    0b10011000u,
    0b11111111u,
    0b00011000u,
    0b00011000u,
    0b00011000u,
    0b00011000u,
    0b00011000u,
    0b00011000u,
)
val five:UByteArray= ubyteArrayOf(
    0b11111111u,
    0b11111111u,
    0b11000000u,
    0b11000000u,
    0b11000000u,
    0b11111111u,
    0b11111111u,
    0b00000011u,
    0b00000011u,
    0b00000011u,
    0b11111111u,
    0b11111111u,
)
val six:UByteArray= ubyteArrayOf(
    0b11111111u,
    0b11111111u,
    0b11000000u,
    0b11000000u,
    0b11000000u,
    0b11111111u,
    0b11111111u,
    0b11000011u,
    0b11000011u,
    0b11000011u,
    0b11111111u,
    0b11111111u,
)
val seven:UByteArray= ubyteArrayOf(
    0b11111111u,
    0b11111111u,
    0b00000011u,
    0b00000110u,
    0b00001100u,
    0b11111111u,
    0b11111111u,
    0b00110000u,
    0b00110000u,
    0b01100000u,
    0b01100000u,
    0b11000000u,
)
val eight:UByteArray= ubyteArrayOf(
    0b11111111u,
    0b11111111u,
    0b11000011u,
    0b11000011u,
    0b11000011u,
    0b11111111u,
    0b11111111u,
    0b11000011u,
    0b11000011u,
    0b11000011u,
    0b11111111u,
    0b11111111u,
)
val nine:UByteArray= ubyteArrayOf(
    0b11111111u,
    0b11111111u,
    0b11000011u,
    0b11000011u,
    0b11000011u,
    0b11111111u,
    0b11111111u,
    0b00000011u,
    0b00000011u,
    0b00000011u,
    0b00000011u,
    0b00000011u,
)
val ten:UByteArray= ubyteArrayOf(
    0b11011111u,
    0b11011111u,
    0b11011011u,
    0b11011011u,
    0b11011011u,
    0b11011011u,
    0b11011011u,
    0b11011011u,
    0b11011011u,
    0b11011011u,
    0b11011111u,
    0b11011111u,
)
val digits=arrayOf(naught,one,two,three,four,five,six,seven,eight,nine,ten)
/*This is a container for the canvas and context, and is in charge for drawing the screen*/
class MyCanvasWindow{
    private val canvas : HTMLCanvasElement= document.createElement("canvas") as HTMLCanvasElement;
    private val canvas_context : CanvasRenderingContext2D = canvas.getContext("2d") as CanvasRenderingContext2D;
    constructor(){
        canvas.width=800; canvas.height=600
        canvas.id="outlaw_canvas"
        body.appendChild(canvas);

    }
    /*this draws the scoreboard digits*/
    fun draw_hits(){
        /*draw Player 1's score*/
        run {
            val hits = sherrif2.gethits()
            canvas_context.fillStyle = sherrif1.color
            /* index the 1 byte lines*/
            for (i in 0..12) {
                /* loop grabs bits starting from the most significant and draws them on screen*/
                var b = digits[hits][i]
                for (j in 0..8) {
                    if (b >= 0x80u) {
                        canvas_context.fillRect(16.0 + j * size_of_dot, i * size_of_dot, size_of_dot, size_of_dot)
                    }
                    b = (b * 2u).toUByte()

                }
            }
        }
        /*draw player 2's score*/
        run {
            val hits = sherrif1.gethits()
            canvas_context.fillStyle = sherrif2.color
            /* index the 1 byte lines*/
            for (i in 0..12) {
                /* loop grabs bits starting from the most significant and draws them on screen*/
                var b = digits[hits][i]
                for (j in 0..8) {
                    if (b >= 0x80u) {

                        canvas_context.fillRect(800.0 - 64.0 - 16.0 + j * size_of_dot, i * size_of_dot, size_of_dot, size_of_dot)
                    }
                    b = (b * 2u).toUByte()

                }
            }
        }

    }
    /*draws the screen once, should be called from an interval*/
    fun draw(){
        canvas_context.fillStyle="#f0f0e7";
        canvas_context.fillRect(0.0,0.0,800.0,600.0);
        canvas_context.fillStyle="#8c7864";
        canvas_context.fillRect(0.0,wall_distance_from_top,800.0,wall_size);
        canvas_context.fillRect(0.0,600.0-wall_size,800.0,wall_size);
        canvas_context.fillStyle="#aaaabb"
        canvas_context.fillRect(0.0,0.0,800.0,wall_distance_from_top);
        sherrif1.drawOn(canvas_context)
        sherrif2.drawOn(canvas_context)
        obstacle.drawOn(canvas_context)
        draw_hits()
    }

}

/* Keep Track of all the Interval values so you can stop moving*/
var movementIntervals : MutableMap<String, Int?> = mutableMapOf<String,Int?>(
    "KeyW" to null,
    "KeyD" to null,
    "KeyA" to null,
    "KeyS" to null,
    "KeyI" to null,
    "KeyJ" to null,
    "KeyK" to null,
    "KeyL" to null
)
/*This function converts keycode names to directions*/
fun getDelta(keycode : String) : Pair<Double,Double>{
    when (keycode) {
        "KeyW","KeyI"-> {return Pair(0.0, -1.0)}
        "KeyS","KeyK"-> {return Pair(0.0, 1.0)}
        "KeyA","KeyJ"-> {return Pair(-1.0,0.0)}
        "KeyD","KeyL"-> {return Pair(1.0,0.0)}
    }
    return Pair(0.0, 0.0)
}
/*This function converts keycode names to which sherrif they are assigned to*/
fun getSherrif(keycode:String) : Sherrif?{
    when(keycode){
        "KeyW","KeyS","KeyA","KeyD","KeyQ" -> {return sherrif1}
        "KeyI","KeyJ","KeyK","KeyL","KeyU" -> {return sherrif2}
    }
    return null;
}
fun keyDownHandler(e:Event){
    val keyboard_event = e as KeyboardEvent;
    if (!keyboard_event.repeat) {
        val keycode = keyboard_event.code;
        when (keycode) {
            /*movement keys for both sherrifs*/
            "KeyW","KeyS","KeyA","KeyD","KeyI","KeyJ","KeyK","KeyL" -> {
                val sherrif=getSherrif(keycode) as Sherrif;
                val delta = getDelta(keycode)
                movementIntervals.put(keycode, sherrif.accelerate(delta.first, delta.second));
                sherrif.aim_fix()
            }
            /*shoot keys for both sherrifs*/
            "KeyQ","KeyU" -> {
                val sherrif=getSherrif(keycode) as Sherrif
                sherrif.aim()
            }


        }
    }

}
fun keyUpHandler(e:Event){
    val keyboard_event = e as KeyboardEvent;
    val keycode=keyboard_event.code;

    when(keycode){
        /*movement keys for both sherrifs*/
        "KeyW", "KeyD", "KeyA", "KeyS","KeyI","KeyK","KeyJ","KeyL" ->{
            val sherrif=getSherrif(keycode) as Sherrif
            sherrif.stop_one_move(keycode);

        }
        /*shoot keys for both sherrifs*/
        "KeyQ","KeyU" -> {
            val sherrif=getSherrif(keycode) as Sherrif
            sherrif.fire();
        }
    }
}

var cw:Int= -2; //This stores what interval checkwin is on
//called from intervals and checks for the win condition
fun checkwin(){
    if ((sherrif1.gethits()>=10) or(sherrif2.gethits()>=10)){

        window.clearTimeout(world_resumer)
        if ((sherrif1.gethits()>=10) and (sherrif2.gethits()>=10)){
            console.log("Tie")
        }else  if ((sherrif1.gethits()>=10)){
            console.log("Player 2 wins")
        }else if ((sherrif2.gethits()>=10)){
            console.log("Player 1 wins")
        } else {
            console.log("Wha?")
        }
        window.clearInterval(cw)
    }
}

fun main() {
    val mywindow=MyCanvasWindow();
    document.addEventListener("keydown", ::keyDownHandler)
    document.addEventListener("keyup", ::keyUpHandler)
    window.setInterval(mywindow::draw,10)
    cw=window.setInterval(::checkwin,400)
    window.setInterval(sherrif1::step_cycle_handler,400)
    window.setTimeout(window::setInterval,200,sherrif2::step_cycle_handler,380)

}
