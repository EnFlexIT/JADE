using System;
using System.Collections;

using jade.core;
using jade.util.leap;

using JADE_HTTP			= jade.mtp.http;

namespace JadeTest
{
	/// <summary>
	/// This class start a new JadeLeap container in C#.
	/// A file named "leap.properties" have to be insert in the project working directory
	/// to read settings for the agent creation.
	/// If that file doesn't exist will be created a container with standard settings.
	/// </summary>
	public class AgentBooterCSharp
	{
		/// <summary>
		/// The main entry point for the application.
		/// </summary>
		
		private static readonly string DEFAULT_FILE = "leap.properties";
		private static Profile p;

		[STAThread]
		public static void Main(string[] args)
		{
			Properties pps = new Properties();

			if ( System.IO.File.Exists(DEFAULT_FILE) )
				LoadLeapPropertiesFile();
			else
				p = new ProfileImpl();

			if (args.Length > 0)
			{
				pps = jade.Boot.parseCmdLineArgs(args);
				
				if (pps != null)
				{
					java.util.Enumeration en = pps.keys();
					while ( en.hasMoreElements() )
					{
						string key = (string) en.nextElement();
						string prop = pps.getProperty(key);
						p.setParameter(key, prop);
					}//End WHILE block
				}//End IF block
			}//End IF block
			
			/* VERSIONE 3.2 */
			jade.wrapper.AgentContainer mc = null;
			/* VERSIONE 3.1 */
			//jade.wrapper.MainContainer mc = null;

			try
			{
				if (p.getParameter("main", "true").Equals("false"))
					mc = Runtime.instance().createAgentContainer(p);
				else
					mc = Runtime.instance().createMainContainer(p);
			}
			catch(Exception exc)
			{
				Console.WriteLine( "\n----------- INIZIO ECCEZIONE ------------------" );
				Console.WriteLine( exc.Message );
				Console.WriteLine( exc.InnerException );
				Console.WriteLine( "\n----------- FINE ECCEZIONE ------------------" );
			}
			Console.ReadLine();
		}


		private static  void LoadLeapPropertiesFile()
		{
			try
			{
				p = new ProfileImpl( DEFAULT_FILE );
			}
			catch(ProfileException pe)
			{
				Console.WriteLine(pe.Message);
				Console.WriteLine("No leap.properties file found!\nUsing default option instead.");
				p = new ProfileImpl();
			}
		}

	}//End AgentBooterCSharp class

}
