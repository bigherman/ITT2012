<?php
        $q=$_GET["icao"];

		@ $db = new mysqli('localhost','web_user','password','metar');

		if (mysqli_connect_errno())
		{
				echo "Error: Could not connect to database";
				exit;
		}

		$query = "select * from reports where icao = '".$q."'";

        $result = $db->query($query);

        $num_results = $result->num_rows;
		
        for ($i = 0; $i <$num_results; $i++)
        {
            $row = $result->fetch_assoc();
				
			$arr = array('icao' => $row['icao'], 'time' => $row['time'], 'report' => str_replace("\n", '', $row['report']), 'colour' => $row['colour']);
				
			/* get the JSON printed */
			echo json_encode($arr);
        }
?>


