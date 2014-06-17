<?php
echo "entered procedure<br/>";

$q=$_GET["icao"];

@ $db = new mysqli('localhost','web_user','*******','metar');

if (mysqli_connect_errno())
{
        echo "Error: Could not connect to database";
        exit;
}

echo "enter query<br/>";

$query = "select * from reports where icao = '".$q."'";

echo "run query<br/>";
$result = $db->query($query);

echo "get number of rows: ";
$num_results = $result->num_rows;
echo "$num_results<br/><br/>";

echo "<table border=1>";
echo "<tr>";
echo "<th>ICAO</th><th>Time</th><th>METAR</th>";
echo "</tr>";
for ($i = 0; $i <$num_results; $i++)
{
        $row = $result->fetch_assoc();
        echo "<tr><td>";
        echo stripslashes ($row['icao']);
        echo "</td><td>";
        echo stripslashes ($row['time']);
        echo "</td><td>";
        echo stripslashes ($row['report']);
        echo "</td></tr>";
}
echo "</table>";
?>
