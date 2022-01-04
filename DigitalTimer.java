import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.lang.Math;

/**
 * A simple digital timer widget made using the 7-segment display.
 * The timer counts down from a given number of seconds (default:60 sec),with a maximum possible number of digits displayed being 5,
 * and the maximum number being 99999. The timer can be reset to the number of seconds it was initialized with, or it can add or subtract
 * the amount of seconds it counts down from, from a maximum of 10<sup>number of digits</sup> (e.g. if there are 4 possible digits
 * on the display, the maximum number is 9999) to 0.
 * The volume of the alarm can be adjusted from 0-100, and the timer colour themes can be 
 * chosen from any of the five below:
 * <p>
 * <img src = "WidgetThemes.png" width = 280 height = 155>
 * <p>
 * Alarms for each theme with credits:<p>
 * <audio controls src="StandardAlarm.wav"></audio>&nbsp;&nbsp;&nbsp;Standard:
 * &nbsp<a href= "https://www.youtube.com/watch?v=LebohQGJJJ0">Youtube Link</a> (00:10 - 00:12), Author: Time Taker<p>
 * <audio controls src="OceanSound.wav"></audio>&nbsp;&nbsp;&nbsp;Ocean:
 * &nbspUmi to Shounen (02:05 - 02:24), Artist: Anzen Chitai<p>
 * <audio controls src="Temptation.wav"></audio>&nbsp;&nbsp;&nbsp;Playful:
 * &nbspTemptation'82 (00:00 - 00:15), Artist: New Order<p>
 * <audio controls src="LonelyNoMore.wav"></audio>&nbsp;&nbsp;&nbsp;Regal:
 * &nbspLonely No More (00:00 - 00:16), Artist: Rob Thomas<p>
 * <audio controls src="ElectricFeel.wav"></audio>&nbsp;&nbsp;&nbsp;Striking:
 * &nbspElectric Feel (00:00 - 00:15), Artist: MGMT<p>
 * 
 * @author Katelyn Lam
 * @version Nov 2021
 */
public class DigitalTimer extends Actor
{
    //dimension of timer screen
    private int width;
    private int height;
    
    private int numDigits; //number of digits projected on screen
    private int numCycles; //number displayed on timer as it is counted down
    private int numCyclesOriginal; //number counted down from
    private int maxNum; //maximum possible number that can be displayed on timer
    
    //timer colours
    private Color backgroundColour;
    private Color onSegmentColour;
    private Color offSegmentColour;
    
    private int volume; //volume of alarm
    
    //visual representation of each digit
    private static boolean[] defaultOffDigit = new boolean[] {false,false,false,false,false,false,false}; //by default, all segments are off
    private static boolean[][] digit = new boolean[][]{ //representing each digit on a 7-segment display
            {true, true, true, true, true, true, false},//digit 0
            {false, true, true, false, false, false, false}, //digit 1
            {true, true, false, true, true, false, true}, //digit 2
            {true, true, true, true, false, false, true}, //digit 3
            {false, true, true, false, false, true, true}, //digit 4
            {true, false, true, true, false, true, true}, //digit 5
            {true, false, true, true, true, true, true}, //digit 6
            {true, true, true, false, false, false, false},//digit 7
            {true, true, true, true, true, true, true}, //digit 8
            {true, true, true, true, false, true,true}}; //digit 9
            
    private boolean[][][] digits; //array of all digits on segment display
    
    //dimensions of a single digit displayed
    private static final int digitWidth = 64;
    private static final int digitHeight = 36;
    
    private int themeIndex; //accessor index for theme
    private static Color[][] timerColours = new Color[][] { //represents possible colour theme for timer
        {new Color(102,102,102), Color.RED, Color.GRAY}, //standard theme
        {new Color(19,79,92), new Color(243,243,243),new Color(42,109,120)}, //ocean theme
        {new Color(103,78,167), new Color(222, 7, 230),new Color(131, 102, 173)}, //playful theme
        {new Color(51, 42, 18),new Color(199,152,56),new Color(105,100,86)}, //regal theme
        {Color.BLACK, Color.GREEN,new Color(38,4,2)}}; //striking theme
        
    private static GreenfootSound alarms[] = new GreenfootSound[]{ //represents sounds for timer themes
        new GreenfootSound("StandardAlarm.wav"), //standard
        new GreenfootSound("OceanSound.wav"), //ocean
        new GreenfootSound("Temptation.wav"), //playful
        new GreenfootSound("LonelyNoMore.wav"), //regal
        new GreenfootSound("ElectricFeel.wav")}; //striking
    private GreenfootSound alarm; //chosen alarm sound, based on theme
    private boolean alarmPlayed; //plays alarm if it is true
    
    private GreenfootImage currentTime; //current time displayed by timer
    private boolean startTimer; //state if timer has been started
    
    //variables to calculate amount of time elapsed
    private long initiatedTime;
    private long elapsedTime;
    private long elapsedTimeAfterUpdate;
    private double elapsedSeconds;
    
    //state of if timer is updated
    private boolean isUpdated;
    

    /**
     * Default constructor for timer.
     * Creates a timer that counts down 60s with standard theme colours, and volume of 80.
     */
    public DigitalTimer()
    {
        this(60,0,80); //initiates timer that counts down 60s, standard theme, and volume of 80
        maxNum = 60; //overwrites maxNum to be 60s
    }
    
    /**
     * Customizable constructor for timer, with selectable theme, countdown number, and volume.
     * @param num number timer counts down from. Maximum possible value of num is 99999.
     * @param theme selected theme for the timer (selected pairing of colours and sound). See the chart above for themes.
     * <code>theme</code> must be an integer from 0 - 4, to select the following themes: <p>
     * &emsp;0 - standard,&nbsp; 1 - ocean,&nbsp; 2 - playful,&nbsp; 3 - regal,&nbsp; 4 - striking
     * @param volume volume of alarm, an integer from 0 (off) - 100 (maximum volume)
     */
    public DigitalTimer(int num, int theme, int volume)
    {
        if(num > 99999)
        {
            numDigits = 5;
            numCycles = 99999;
        }
        else
        {
            numDigits = findGreatestPowerOfTen(num) + 1;
            numCycles = num;
        }
        maxNum = (int)Math.pow(10,numDigits) - 1;
        width = 32 * numDigits;
        height = 36;
        numCyclesOriginal = numCycles;
        themeIndex = theme;
        startTimer = false;
        isUpdated = false;
        backgroundColour = timerColours[themeIndex][0];
        this.volume = volume;
        alarm = alarms[themeIndex];
        alarmPlayed = false;
        alarm.setVolume(volume);
        initializeDigits();
        currentTime = drawTimer(width,height,numDigits,backgroundColour,themeIndex,numCycles,digits);
        setImage(currentTime);
    }
    
    /**
     * Counts down from initialized value (60 for basic timer, user inputted option for complex timer) to 0
     * by retrieving number of milliseconds since Jan 01, 1970 to find the elapsed time, and refreshing timer screen every 1s passed.
     * Note: there is an allowable margin of error of +/- 0.01s every time "1s" has been elapsed before the timer refreshes.
     * (Thank you Mr. Cohen [teacher at PETHS] for helping me on this one.)
     */
    public void act() 
    {
      //find time elapsed in seconds
      elapsedTime = System.currentTimeMillis() - initiatedTime;
      elapsedSeconds = (double)elapsedTime/1000.0;
      
      //updates timer to reflect number of seconds left from start, plays alarm when 0 is reached
      if(startTimer) 
      {
          //draws the number of seconds that has elapsed on timer
         if(numCycles > 0)
            currentTime = drawTimer(width, height,numDigits,backgroundColour,themeIndex,numCycles,digits);
         else
             currentTime = drawTimer(width,height,numDigits,backgroundColour,themeIndex,0,digits);
     
         //plays alarm only if it has not been stopped by the user and if counted down to 0
         if(numCycles == 0 || alarmPlayed)
            alarm.playLoop();
         
         //allows for a 0.01s margin of error
         if(elapsedSeconds >= 0.99 && elapsedSeconds <= 1.01)
         {
             numCycles--;
             initiatedTime = System.currentTimeMillis();
         }
         
      }
      
      //only allows timer update if it has not already occured in current counted second
      if(isUpdated)
      {
          if(System.currentTimeMillis() - elapsedTimeAfterUpdate > 1000)
            isUpdated = false;
      }
      setImage(currentTime);
    }
    
    
    /**
     * Starts timer countdown
     */
    public void start()
    {
        startTimer = true;
        initiatedTime = System.currentTimeMillis();        
    }
    
    /**
     * Stops timer and displays time it stopped at
     */
    public void stop()
    {
        startTimer = false;
    }
    
    /**
     * Restarts timer to original time it was initialized with
     */
    public void update()
    {
        if(!isUpdated)
        {
            alarm.stop();
            numCycles = numCyclesOriginal;
            currentTime = drawTimer(width,height,numDigits,backgroundColour,themeIndex,numCycles,digits);
            startTimer = false;
        }
        //ensures timer does not update multiple times within same act cycle
        elapsedTimeAfterUpdate = System.currentTimeMillis();
        isUpdated = true;
        alarmPlayed = false;
        
    }
    
    /**
     * Adds a given number of cycles, as an integer for timer to count down. 
     * @param numAddedCycles number of cycles added to extend the timer. If is negative,
     * that time is subtracted from the number of cycles counted. If the amount of extended
     * time exceeds the cap (60 for simple timer, 10<sup>number of digits</sup> - 1 for complex
     * timer), then resets timer to cap. Otherwise, if <code>numAddedCycles</code> when subtracted
     * from the current number of act cycles is less than 0, the timer is set to 0.
     */
    public void update(int numAddedCycles)
    {
        if(!isUpdated)
        {
            alarm.stop();
            int changedCycles = numCycles + numAddedCycles + 1;
            if(changedCycles > maxNum)
                numCycles = maxNum;
                
            else if(changedCycles < 0)
                numCycles = 0;
            else
                numCycles = changedCycles;
            
            currentTime = drawTimer(width, height,numDigits,backgroundColour,themeIndex,numCycles,digits);
        }
        //ensures timer does not update multiple times within same act cycle
        elapsedTimeAfterUpdate = System.currentTimeMillis();
        isUpdated = true;
        alarmPlayed = false;
    }
    
    /**
     * Initializes the array of digits that can be displayed for the timer's size
     */
    private void initializeDigits()
    {
        digits = new boolean[numDigits][10][7];
        for(int i = 0; i < numDigits; i++)
        {
            digits[i] = digit;
        }
    }
    
    /**
     * Gets the number of seconds left on the timer since the timer has started
     * @return int number of seconds that is left since timer has started. Must be less than number of cycles initialized.
     */
    public int getNumCyclesLeft()
    {
        if(numCycles > 0)
            return numCycles + 1;
        else
            return 0;
    }
    
    
    /**
     * Turns off alarm when it is sounding, which is when the timer has finished counting down
     */
    public void turnOffAlarm()
    {
        alarm.stop();
        alarmPlayed = false;
    }
    
    /**
     * Draws timer 
     * @param width - width of timer backboard
     * @param height - height of timer backboard
     * @param numDigits - number of digits displayed on board
     * @param colour - colour of background
     * @param themeIndex - index to access theme in timerColours
     * @param num - number depicted on display
     * @param digitSequence - array of LED sequence for number displayed
     */
    private static GreenfootImage drawTimer(int width, int height,int numDigits, Color colour, int themeIndex,int num, boolean[][][] digitSequence)
    {
        int drawX = width/16;
        int drawY= height/9;
        int numDigitsDisplayed = 0;
        int greatestPowerOfTen = 0;
        int loopingNum = num;
        int digitNum = num;
   
        
        numDigitsDisplayed = numDigits - 1;
        greatestPowerOfTen = findGreatestPowerOfTen(num) + 1;
        int[] digitsDisplayed = new int[numDigits];
        
        //finds number of digits that the given number has
        for(int j = 0; j < numDigits; j++)
        {
            if(num < (Math.pow(10,numDigitsDisplayed)))
                digitsDisplayed[j] = 0;
            else
            {
                digitNum = loopingNum/(int)(Math.pow(10,numDigitsDisplayed));
                digitsDisplayed[j] = digitNum;
                loopingNum = loopingNum - digitNum * (int)(Math.pow(10,numDigitsDisplayed));
            }
            numDigitsDisplayed--;
        }
        
        greatestPowerOfTen = numDigits - greatestPowerOfTen;
        
        GreenfootImage timer = new GreenfootImage(width, height);
        timer.setColor(colour);
        timer.fillRect(0,0,width,height);
        
        /*fits the number to the size of the timer. For ex. if the number is 23 but the timer size accomodates
        three digits, then the number is represented as 023 where the 0 in front of the 23 has all segments off*/
        for(int i = 0; i < numDigits; i++)
        {
            if(i < greatestPowerOfTen && digitsDisplayed[i] == 0 && numDigits > 1)
                timer.drawImage(drawDigit(digitWidth/2,digitHeight,defaultOffDigit,themeIndex),drawX, drawY);
            else
                timer.drawImage(drawDigit(digitWidth/2,digitHeight,digitSequence[i][digitsDisplayed[i]],themeIndex),drawX, drawY);
            drawX = drawX + (7 * digitWidth/16);
        }
        return timer;
    }
    /**
     * Draws a single digit
     * @param width - width of timer backboard digit is drawn on
     * @param height - height of timer backboard digit is drawn on
     * @param onSegment - array of on/off sequences for digit
     * 
     */
    private static GreenfootImage drawDigit(int width, int height, boolean[] onSegment, int themeIndex)
    {
        int[] wideSegmentDimensions = new int[]{width/2,height/9};
        int[] longSegmentDimensions = new int[]{width/8,2*height/9};
        GreenfootImage drawnDigit = new GreenfootImage(3 * width / 4, 7 * height /9);
        drawnDigit.setColor(pickColour(onSegment[0],timerColours[themeIndex][1],timerColours[themeIndex][2]));
        drawnDigit.fillRect(width/8,0,wideSegmentDimensions[0],wideSegmentDimensions[1] );
        drawnDigit.setColor(pickColour(onSegment[1],timerColours[themeIndex][1],timerColours[themeIndex][2]));
        drawnDigit.fillRect(3 * width / 4 - width/8,height/9 + 1,longSegmentDimensions[0],longSegmentDimensions[1] );
        drawnDigit.setColor(pickColour(onSegment[2],timerColours[themeIndex][1],timerColours[themeIndex][2]));
        drawnDigit.fillRect(3 * width / 4 - width/8,4 * height/9 + 1,longSegmentDimensions[0], longSegmentDimensions[1]);
        drawnDigit.setColor(pickColour(onSegment[3],timerColours[themeIndex][1],timerColours[themeIndex][2]));
        drawnDigit.fillRect(width/8,2 * height/3,wideSegmentDimensions[0],wideSegmentDimensions[1] );
        drawnDigit.setColor(pickColour(onSegment[4],timerColours[themeIndex][1],timerColours[themeIndex][2]));
        drawnDigit.fillRect(0,4 * height/9 + 1,longSegmentDimensions[0],longSegmentDimensions[1] );
        drawnDigit.setColor(pickColour(onSegment[5],timerColours[themeIndex][1],timerColours[themeIndex][2]));
        drawnDigit.fillRect(0, height/9 + 1,longSegmentDimensions[0], longSegmentDimensions[1]);
        drawnDigit.setColor(pickColour(onSegment[6],timerColours[themeIndex][1],timerColours[themeIndex][2]));
        drawnDigit.fillRect(width/8,1 * height/3 + 1,wideSegmentDimensions[0],wideSegmentDimensions[1] );
        return drawnDigit;
    }
    
    /**
     * Picks colour, providing options for segment on and off
     * @param value - state of whether a given segment is on or off
     * @param onColour - colour of segment if it is on
     * @param offColour - colour of segment if it is off
     */
    private static Color pickColour(boolean value, Color onColour, Color offColour)
    {
        if(value)
            return onColour;
        else
            return offColour;
    }
    
    /**
     * Finds greatest power of ten that is less than or equal to the number
     * and returns this value to the caller
     * @param num - the number for which the greatest power of ten is found
     * @return int - the greatest power of 10, or log(num) rounded to the lowest int
     */
    private static int findGreatestPowerOfTen(int num)
    {
        int greatestPowerOfTen = 0;
        
        while(num/(int)Math.pow(10,greatestPowerOfTen) >= 10)
        {
            greatestPowerOfTen++;
        }
        return greatestPowerOfTen;
    }
        
}
