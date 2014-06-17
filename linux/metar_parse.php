<?php
class metarRegEx
{
    // the string to parse
    private $string;
	
	// all values
    private $cloudAltitude, $visibilityMeters;
	public $colourState;
	
	private $EXP_CLOUDS = '/\s(SCT|BKN|OVC)[0-9]{3}\s|\s(SCT|BKN|OVC)[0-9]{3}\/+/'; // cloud density and altitude
	private $EXP_VISIBILITY = '/\s[0-9]{4}\s|\s[0-9]{4}NDV\s|\s(\d\/\dSM)\s|\s[0-9]{1,4}SM\s/'; // visibility
	private $EXP_CAVOK = '/\sCAVOK\s/'; //Cloud and Visibility OK (CAVOK)
	
	//---------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------
    function metarRegEx($string)
    {
        $this->string = $string;

        $this->setValues();
    }
    //---------------------------------------------------------------------
	
	    //---------------------------------------------------------------------
    // Use regular expression to parse string and get 
    // relevant values
    //---------------------------------------------------------------------
    private function setValues()
    {
        $colour = -1;
		
		$CAVOKPart = $this->regEx($this->EXP_CAVOK, false);
		if ($CAVOKPart)
		{
			$this->colourState="BLU";
			return;
		}
		
		$cloudPart = $this->regEx($this->EXP_CLOUDS, false);
        if ($cloudPart != null)
        {
            $this->cloudAltitude = substr($cloudPart, 3,3) * 100;
			
			if($this->cloudAltitude<200)
			{
				$colourCld=0;
			}
			else if ($this->cloudAltitude<300)
			{
				$colourCld=1;
			}
			else if ($this->cloudAltitude<700)
			{
				$colourCld=2;
			}
			else if ($this->cloudAltitude<1500)
			{
				$colourCld=3;
			}
			else if ($this->cloudAltitude<2500)
			{
				$colourCld=4;
			}
			else
			{
				$colourCld=5;
			}
        }
		else
		{
			$colourCld=5;
		}
		
        //----- Visibility
        $visibilityPart = $this->regEx($this->EXP_VISIBILITY, false) ;
        if ($visibilityPart != null)
        {
            $value = (substr($visibilityPart, 0, 4) * 1);
            if (strpos($visibilityPart, "SM") !== false)
            {   // statute miles
                $this->visibilityMeters = round($value * 1609.344, 0);
            }
            else
            {   // meters
                $this->visibilityMeters = $value;
            }
			
			if($this->visibilityMeters<800)
			{
				$colourVis=0;
			}
			else if ($this->visibilityMeters<1600)
			{
				$colourVis=1;
			}
			else if ($this->visibilityMeters<3700)
			{
				$colourVis=2;
			}
			else if ($this->visibilityMeters<5000)
			{
				$colourVis=3;
			}
			else if ($this->visibilityMeters<8000)
			{
				$colourVis=4;
			}
			else
			{
				$colourVis=5;
			}
        }
		else
		{
			$colourVis=-1;
		}
		
		$colour=min($colourVis, $colourCld);
		
		if($colour==0)
		{
			$this->colourState="RED";
		}
		else if($colour==1)
		{
			$this->colourState="AMB";
		}
		else if($colour==2)
		{
			$this->colourState="YLO";
		}
		else if($colour==3)
		{
			$this->colourState="GRN";
		}
		else if($colour==4)
		{
			$this->colourState="WHT";
		}
		else if($colour==5)
		{
			$this->colourState="BLU";
		}
		else
		{
			$this->colourState="NIL";
		}
    }
    //---------------------------------------------------------------------


    //---------------------------------------------------------------------
    // Use regular expression to return a value
    // returns array of matches, if $multi is set to true
    //---------------------------------------------------------------------
    private function regEx($expression, $multi = false)
    {
        preg_match($expression, $this->string, $matches);
        
        if ($multi)
          return $matches;
        else
          return (isset($matches[0])) ? trim($matches[0]) :  null;
    }
    //---------------------------------------------------------------------
}

$db=new mysqli('localhost','local_user','*****','metar');

if(mysqli_connect_errno())
{
   echo "Error connecting to database\n";
   exit;
}

$metar_file=fopen("./metars/".gmdate('H')."Z.TXT", "r");
echo "Opened ".gmdate('H')."Z.TXT\n";

while($metar=fgets($metar_file, 500))
{
   if(preg_match("/[A-Z]{4}/",substr($metar,0,4)))
   {
      $icao=substr($metar,0,4);
      $day=substr($metar,5,2);
      $time=substr($metar,7,4);
      $timezone=substr($metar,11,1);
      $report=substr($metar,13);
	  
	  // create new geonamesRegEx object, which parses the string
	  $r = new metarRegEx($report);
	 
      $query="REPLACE INTO reports VALUES('$icao','$time','$report','$r->colourState')";
      echo $icao."\n";
      echo $r->colourState." from main\n";
      $result=$db->query($query);
      //echo $report."\n";
   }
}
?>
