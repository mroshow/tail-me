using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Diagnostics;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace TailMe
{
    public partial class help : Form
    {
        public help()
        {
            InitializeComponent();
            linkLabel1.Links.Remove(linkLabel1.Links[0]);
            linkLabel1.Links.Add(0, linkLabel1.Text.Length, "https://code.google.com/p/tail-me/");
            description1.Text = "This application is intended for use with the TailMe Android app. \n" +
                                "Head over to";
            label2.Text = "and download the Android app to record";
            label3.Text = "some paths. Then just simply plug your phone in to view your paths.\n" +
                "Its that simple :).";
        }

        private void close_button_Click(object sender, EventArgs e)
        {
            
            this.Close();
        }

        private void linkLabel1_LinkClicked(object sender, LinkLabelLinkClickedEventArgs e)
        {
            ProcessStartInfo sInfo = new ProcessStartInfo(e.Link.LinkData.ToString());
            Process.Start(sInfo);
        }
    }
}
