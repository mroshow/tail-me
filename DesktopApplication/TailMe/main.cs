using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using GMap.NET.WindowsForms;
using GMap.NET;
using GMap.NET.MapProviders;
using System.IO;
using System.Runtime.InteropServices;

namespace TailMe
{
    public partial class main : Form
    {
        // Windows message and device event codes from: http://msdn.microsoft.com/en-us/library/windows/desktop/aa363480(v=vs.85).aspx
        const int WM_DEVICECHANGE = 0x0219; // system detected a device change
        const int DBT_DEVICEARRIVAL = 0x8000; // system detected a new device event
        const int DBT_DEVICEREMOVECOMPLETE = 0x8004; //device was removed
        const int DBT_DEVNODES_CHANGED = 0x0007; //device changed

        // Create map control, map overlay and map route
        private GMapControl map = new GMap.NET.WindowsForms.GMapControl();
        private GMapOverlay tempOverlay;
        private GMapRoute tempRoute;

        // Various lists to hold data
        private List<PointLatLng> importPoints;
        private List<double> averageSpeed;
        private List<double> distance;
        private List<int> averageAccuracy;
        private List<GMapOverlay> overlays;
        private List<int> before;
        private List<int> after;
        private List<PointReduction.PointD> routePoints;
        private List<List<PointReduction.PointD>> paths;

        public main()
        {
            InitializeComponent();
            comboBox_maps.DataSource = GMapProviders.List;
            comboBox_maps.SelectedIndex = 1;
            map.MapProvider = (GMapProvider)comboBox_maps.SelectedItem;
            addMap();
            populateDriveList();

            overlays = new List<GMapOverlay>();
            paths = new List<List<PointReduction.PointD>>();
            before = new List<int>();
            after = new List<int>();
            averageSpeed = new List<double>();
            averageAccuracy = new List<int>();
            importPoints = new List<PointLatLng>();
            distance = new List<double>();

            // Creating context menu for right clicking on paths
            this.checkedListBox1.ContextMenuStrip = new ContextMenuStrip();
            this.checkedListBox1.ContextMenuStrip.Opening += new CancelEventHandler(this.MenuOpening);
            this.checkedListBox1.ContextMenuStrip.Items.Add("Path Information");
            this.checkedListBox1.ContextMenuStrip.Items[0].Click += new EventHandler(this.MenuOptionClicked);
        }

        #region Right click menu Control
        // Check if Item is selected before displaying the menu
        private void MenuOpening(object sender, CancelEventArgs e)
        {
            if (this.checkedListBox1.SelectedIndex == -1)
                e.Cancel = true;
        }
        // Performing action when menu item clicked
        private void MenuOptionClicked(object sender, EventArgs e)
        {
            if (this.checkedListBox1.SelectedIndex > -1)
            {
                if (Properties.Settings.Default.enable_pr == true)
                {
                    MessageBox.Show("Number of points before Point reduction:  "
                    + before[this.checkedListBox1.SelectedIndex] + " points\nNumber of points before Point reduction:  "
                    + after[this.checkedListBox1.SelectedIndex] + " points\nAverage Speed: " +
                    Math.Round(averageSpeed[this.checkedListBox1.SelectedIndex], 2) + " meters/s\nAverage Accuracy: " +
                    averageAccuracy[this.checkedListBox1.SelectedIndex] + " meters\nDistance: " +
                    Math.Round(distance[this.checkedListBox1.SelectedIndex], 2) + " K/m", "path Information");
                }
                else
                {
                    MessageBox.Show("Number of points:  "
                    + before[this.checkedListBox1.SelectedIndex] + " points\nAverage Speed: " +
                    Math.Round(averageSpeed[this.checkedListBox1.SelectedIndex], 2) + " meters/s\nAverage Accuracy: " +
                    averageAccuracy[this.checkedListBox1.SelectedIndex] + " meters\nDistance: " +
                    Math.Round(distance[this.checkedListBox1.SelectedIndex], 2) + " K/m", "path Information");
                }
            }
        }
        #endregion

        #region Listening for removeable media, populate combobox
        // Constatly listening for a windows message
        protected override void WndProc(ref Message m)
        {
            // If the message is WM_DEVICECHANGE with the correct parameters of removable media
            if (m.Msg == WM_DEVICECHANGE &&
                 m.WParam.ToInt32() == DBT_DEVICEARRIVAL
                || m.WParam.ToInt32() == DBT_DEVICEREMOVECOMPLETE
                || m.WParam.ToInt32() == DBT_DEVNODES_CHANGED)
            {
                if (m.WParam.ToInt32() != DBT_DEVNODES_CHANGED)
                {
                    populateDriveList(); // populate the Drive selection list
                }
            }
            base.WndProc(ref m);
        }
        //  Get the removable system drives and populate the combobox
        private void populateDriveList()
        {
            var drives = from drive in DriveInfo.GetDrives()
                         where drive.DriveType == DriveType.Removable
                         select drive;

            comboBox1.DataSource = drives.ToList();
        }
        private void main_Load(object sender, EventArgs e)
        {

        }
        #endregion

        #region Adding map, map control
        private void addMap()
        {
            map.Bearing = 0F;
            map.CanDragMap = true;
            map.LevelsKeepInMemmory = 5;
            map.Location = new System.Drawing.Point(12, 87);
            map.SetCurrentPositionByKeywords(Properties.Settings.Default.location); // Centering the map on the user defined location
            map.MarkersEnabled = true;
            map.BorderStyle = BorderStyle.FixedSingle;
            map.MouseWheelZoomType = GMap.NET.MouseWheelZoomType.MousePositionAndCenter;
            map.Name = "map";
            map.PolygonsEnabled = true;
            map.RoutesEnabled = true;
            map.Size = new System.Drawing.Size(960, 511);
            map.Load += new System.EventHandler(this.main_Load);
            map.MinZoom = 3;
            map.MaxZoom = 17;
            map.Zoom = 5;
            map.Manager.Mode = AccessMode.ServerAndCache;
            Controls.Add(map);
            this.ActiveControl = map;   // Set the map as the focused object in the GUI
        }

        private void comboBox_maps_SelectedIndexChanged(object sender, EventArgs e)
        {
            map.MapProvider = (GMapProvider)comboBox_maps.SelectedItem;
        }

        // Enabling/Disabling paths when they are selected/deselected in the checkbox
        private void checkedListBox1_ItemCheck(object sender, ItemCheckEventArgs e)
        {
            int i = checkedListBox1.SelectedIndex;
            if (checkedListBox1.GetItemChecked(i) == true)
            {
                overlays[checkedListBox1.SelectedIndex].IsVisibile = false;

            }
            else if (checkedListBox1.GetItemChecked(i) == false)
            {
                overlays[checkedListBox1.SelectedIndex].IsVisibile = true;
                map.ZoomAndCenterMarkers(overlays[checkedListBox1.SelectedIndex].Id);
            }
        }
        #endregion

        #region Read in the Csv
        private List<string> getFiles()
        {
            String[] temp = Directory.GetFiles(comboBox1.SelectedValue.ToString() + "\\TailMe\\", "*.csv");
            List<string> fileList = temp.ToList();
            for (int i = 0; i < fileList.Count; i++)
            {
                FileInfo fInfo = new FileInfo(fileList[i]);
                if (fInfo.Length == 0)  //Checking for empty files
                {
                    fileList.RemoveAt(i);
                }
            }
            return fileList;
        }

        private void readcsv()
        {
            try
            {
                List<string> fileList = (getFiles());    // Get the files from the directory
                List<double> speed = new List<double>();
                List<double> accuracy = new List<double>();

                for (int i = 0; i < fileList.Count; i++)
                {
                    var reader = new StreamReader(File.OpenRead(fileList[i]));
                    routePoints = new List<PointReduction.PointD>();
                    //opening each file 
                    while (!reader.EndOfStream)
                    {
                        var line = reader.ReadLine();
                        var values = line.Split(',');

                        // Add the provider, speed and accuracy here too and then run calculations on them.
                        importPoints.Add(new PointLatLng(double.Parse(values[1]), double.Parse(values[2])));
                        routePoints.Add(new PointReduction.PointD(double.Parse(values[1]), double.Parse(values[2])));
                        speed.Add(double.Parse(values[4]));


                        if (values[3] != "0.0")
                        {
                            accuracy.Add(double.Parse(values[3]));
                        }

                    }


                    distance.Add(calculateDistance(importPoints));


                    averageAccuracy.Add(Convert.ToInt32(accuracy.Average()));

                    averageSpeed.Add(speed.Average());
                    speed.Clear();   // Clear the temp speed list before reading in the next file
                    accuracy.Clear();    // Clear the temp accuracy list before reading in the next file
                    paths.Add(routePoints); //Add the routepoints list to the path list

                    if (Properties.Settings.Default.enable_pr == true)
                    {
                        reducePoints(i);
                    }
                    else
                    {
                        before.Add(paths[i].Count);

                        tempRoute = new GMapRoute(importPoints, "my route");
                        tempOverlay = new GMapOverlay(map, "OverlayTest");
                        // Add the markers to the start and end of the path
                        tempOverlay.Markers.Add(new GMap.NET.WindowsForms.Markers.GMapMarkerGoogleGreen(importPoints[0]));
                        tempOverlay.Markers.Add(new GMap.NET.WindowsForms.Markers.GMapMarkerGoogleRed(importPoints[importPoints.Count - 1]));
                        tempOverlay.Routes.Add(tempRoute);
                        overlays.Add(tempOverlay);
                        overlays[overlays.Count - 1].IsVisibile = false;
                        map.Overlays.Add(overlays[overlays.Count - 1]);
                        importPoints.Clear();

                    }

                    string temp = fileList[i].Replace(comboBox1.SelectedValue.ToString() + "\\TailMe\\", "");
                    checkedListBox1.Items.Add(temp);
                    clear_button.Enabled = true;
                    import_button.Enabled = false;
                }
            }
            catch
            {
                MessageBox.Show("Uh-oh.\n" + "Directory does not contain any log files." + "\nMake sure your phone is connected and USB storage is enabled.", "Uh-oh",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Asterisk);
            }
        }
        #endregion

        //Point reduction 
        private void reducePoints(int input)
        {
            List<PointReduction.PointD> output = new List<PointReduction.PointD>();

            before.Add(paths[input].Count);

            output = PointReduction.ReducingPoints(paths[input], Properties.Settings.Default.tolerance);    // Running point reduction on the paths
            after.Add(output.Count);

            importPoints.Clear();
            for (int u = 0; u < output.Count; u++)
            {
                importPoints.Add(new PointLatLng(output[u].X, output[u].Y));
            }

            tempRoute = new GMapRoute(importPoints, "my route");
            tempOverlay = new GMapOverlay(map, "OverlayTest");
            // Add the markers to the start and end of the path
            tempOverlay.Markers.Add(new GMap.NET.WindowsForms.Markers.GMapMarkerGoogleGreen(importPoints[0]));
            tempOverlay.Markers.Add(new GMap.NET.WindowsForms.Markers.GMapMarkerGoogleRed(importPoints[importPoints.Count - 1]));

            tempOverlay.Routes.Add(tempRoute);
            overlays.Add(tempOverlay);
            overlays[overlays.Count - 1].IsVisibile = false;
            map.Overlays.Add(overlays[overlays.Count - 1]);
            importPoints.Clear();
        }

        // Calculating the distance of the path using the Haversine formula
        private double calculateDistance(List<PointLatLng> inputList)
        {
            /*  Formula implemented with help from:
             *   Serge Meunier @ smokycogs.com
             *   http://www.smokycogs.com/blog/finding-the-distance-between-two-gps-coordinates/
             */

            double R = 6371;    // Radius of the earth
            double distance = 0;

            for (int i = 0; i < inputList.Count - 1; i++)
            {
                double Lat = ((inputList[i].Lat * (Math.PI / 180)) - (inputList[i + 1].Lat * (Math.PI / 180)));
                double Lng = ((inputList[i].Lng * (Math.PI / 180)) - (inputList[i + 1].Lng * (Math.PI / 180)));

                // a = sin²(delta Lat / 2) + cos(Lat1) * cos(lat2) * sin²(delta Long / 2)
                // c = 2 * ( Angle between (Sqrt of a), (Sqrt of (1-a)) )
                // Distance = Radius of the earth * c

                double a = Math.Sin(Lat / 2) * Math.Sin(Lat / 2) +
                    Math.Cos((inputList[i].Lat * (Math.PI / 180))) * Math.Cos((inputList[i + 1].Lat * (Math.PI / 180))) *
                    Math.Sin(Lng / 2) * Math.Sin(Lng / 2);
                double c = 2 * Math.Atan2(Math.Sqrt(a), Math.Sqrt(1 - a));

                distance += R * c;

            }

            return distance;
        }

        #region Button Actions
        // Display the about dialog
        private void button_about_Click(object sender, EventArgs e)
        {
            About about = new About();
            about.ShowDialog();
        }
        // Clear the imported paths
        private void clear_button_Click(object sender, EventArgs e)
        {
            for (int x = 0; x < overlays.Count; x++)
            {
                overlays[x].IsVisibile = false;
                map.Overlays.Remove(overlays[x]);
            }
            averageSpeed.Clear();
            averageAccuracy.Clear();
            overlays.Clear();
            after.Clear();
            importPoints.Clear();
            before.Clear();
            checkedListBox1.Items.Clear();
            distance.Clear();
            clear_button.Enabled = false;
            import_button.Enabled = true;
        }
        // Launch the help box
        private void help_button_Click(object sender, EventArgs e)
        {
            help help = new help();
            help.Show();
        }
        // Launch the settings Dialog
        private void settings_button_Click(object sender, EventArgs e)
        {
            Settings settings = new Settings();
            settings.Show();
        }
        // Import paths from the phone.
        private void import_button_Click(object sender, EventArgs e)
        {
            readcsv();
        }
        #endregion

        // Exit the application
        private void main_FormClosed(object sender, FormClosedEventArgs e)
        {
            Application.Exit();
        }
    }
}
