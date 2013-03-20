using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace TailMe
{
    public partial class Settings : Form
    {
        public Settings()
        {
            InitializeComponent();
            checkButtonState();
            pr_description.Text = "The tolerance value will vary the point reduction \nresults. A larger number will produce less points."+
                " \n\n                                      Default value = 0.0001";
            tolerance_textbox.Text = Properties.Settings.Default.tolerance.ToString();
            location_textbox.Text = Properties.Settings.Default.location;
            checkBox1.Checked = Properties.Settings.Default.enable_pr;
        }

        private void exit_button_Click(object sender, EventArgs e)
        {
            Properties.Settings.Default.tolerance = Double.Parse(tolerance_textbox.Text);
            Properties.Settings.Default.location = location_textbox.Text;
            Properties.Settings.Default.enable_pr = checkBox1.Checked;
            Properties.Settings.Default.Save();
            this.Close();
        }

        private void checkButtonState()
        {
            if (checkBox1.Checked == true)
            {
                tolerance_textbox.Enabled = true;
            }
            if (checkBox1.Checked == false)
            {
                tolerance_textbox.Enabled = false;
            }
        }

        private void cancel_button_Click(object sender, EventArgs e)
        {
            this.Close();
        }

        private void checkBox1_CheckedChanged(object sender, EventArgs e)
        {
            if (checkBox1.Checked == true)
            {
                tolerance_textbox.Enabled = true;
            }
            if (checkBox1.Checked == false)
            {
                tolerance_textbox.Enabled = false;
            }
        }

    }
}
