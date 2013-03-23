using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TailMe
{
    /* Douglas-Peucker Line Approximation Algorithm provided by:
     * CraigSelbert @ codeproject.com
     * http://www.codeproject.com/Articles/18936/A-C-Implementation-of-Douglas-Peucker-Line-Approxi
     */
    class PointReduction
    {
        // Defining a Double Point structure
        public struct PointD
        {
            public double X;
            public double Y;
            public PointD(double x, double y)
            {
                X = x;
                Y = y;
            }
        }

        public static Double DistanceBetweenPoints(PointD P1, PointD P2, PointD P)
        {
            // Area of a triangle - Half the base by the perpenducular height
            Double area = Math.Abs(.5 * (P1.X * P2.Y + P2.X * P.Y + P.X * P1.Y - P2.X * P1.Y - P.X * P2.Y - P1.X * P.Y));

            // Base of Triangle
            Double bottom = Math.Sqrt(Math.Pow(P1.X - P2.X, 2) + Math.Pow(P1.Y - P2.Y, 2));

            // perpendicular height of Triangle
            Double perpendicularHeight = area / bottom * 2;
            return perpendicularHeight;
        }

        public static List<PointD> ReducingPoints(List<PointD> Points, Double Tolerance)
        {
            //Check to see if path is already reduced to a minimum of 3 points
            if (Points.Count < 3)
            {
                return Points;
            }          
            // Create start and endpoint.
            int startPoint = 0;
            int endPoint = Points.Count - 1;
            List<int> pointsToKeep = new List<int>();   // List of points to keep

            //Add the first and last index to the keepers
            pointsToKeep.Add(startPoint);
            pointsToKeep.Add(endPoint);
            // Perform the pointReduction
            performReduction(Points, startPoint, endPoint, Tolerance, pointsToKeep);

            List<PointD> output = new List<PointD>();   // List of points to output

            pointsToKeep.Sort(); // Sort the list after point reduction

            // Add eachelement of pointsToKeep to the PointD output list
            foreach (int index in pointsToKeep)
            {
                output.Add(Points[index]);
            }
            return output;
        }

        private static void performReduction(List<PointD> points, int firstPoint, int lastPoint, Double tolerance, List<int> pointsToKeep)
        {
            Double farthestDistance = 0;
            int indexFarthest = 0;

            /*
             * The algorithm recursively calls itself to divide the line.
             * It then finds the point that is furthest from the current line segment.
             */

            for (int index = firstPoint; index < lastPoint; index++)
            {
                Double distance = DistanceBetweenPoints(points[firstPoint], points[lastPoint], points[index]);
                if (distance > farthestDistance)
                {
                    farthestDistance = distance;
                    indexFarthest = index;
                }
            }

            if (farthestDistance > tolerance && indexFarthest != 0)
            {
                pointsToKeep.Add(indexFarthest);    // Keep the point furthest from the tolerance

                performReduction(points, firstPoint, indexFarthest, tolerance, pointsToKeep);
                performReduction(points, indexFarthest, lastPoint, tolerance, pointsToKeep);
            }
        }
    }
    
}
