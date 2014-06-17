<?php
        $q=$_GET["icao"];

		@ $db = new mysqli('localhost','web_user','*******','metar');

		if (mysqli_connect_errno())
		{
				echo "Error: Could not connect to database";
				exit;
		}

		$query = "select * from reports where icao = '".$q."'";

        $result = $db->query($query);

        $num_results = $result->num_rows;

        header('Content-type: text/xml');
        header('Pragma: public');
        header('Cache-control: private');
        header('Expires: -1');

        /* create a dom document with encoding utf8 */
        $domtree = new DOMDocument('1.0', 'UTF-8');

        /* create the root element of the xml tree */
        $xmlRoot = $domtree->createElement("xml");
        /* append it to the document created */
        $xmlRoot = $domtree->appendChild($xmlRoot);

        for ($i = 0; $i <$num_results; $i++)
        {
                $row = $result->fetch_assoc();

                $currentReport = $domtree->createElement("report");
                $currentReport = $xmlRoot->appendChild($currentReport);

                $currentReport->appendChild($domtree->createElement('icao',$row['icao']));
                $currentReport->appendChild($domtree->createElement('time',$row['time']));
                $currentReport->appendChild($domtree->createElement('report',$row['report']));
        }
        /* get the xml printed */
        echo $domtree->saveXML();
?>


