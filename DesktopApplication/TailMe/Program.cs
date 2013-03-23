using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace TailMe
{
    static class Program
    {
        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        static void Main()
        {
            Application.EnableVisualStyles();
            // Check to see if the splashscreen is enabled before deciding what to launch.
            Application.SetCompatibleTextRenderingDefault(false);
            if (Properties.Settings.Default.show_splash == true)
            {
                Application.Run(new splash());
            }
            else
            {
                Application.Run(new main());
            }
            
        }
    }
}
