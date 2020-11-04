import System.*;

import jade.core.*;import jade.util.leap.*;

import jade.mtp.http.*;

	public class AgentBooterJSharp
	{
		/// <summary>
		/// The main entry point for the application.
		/// </summary>
		
		private static String DEFAULT_FILE = "leap.properties";
		private static Profile p;

		public static void main(String[] args)
		{
			Properties pps = new Properties();

			if ( System.IO.File.Exists(DEFAULT_FILE) )
				LoadLeapPropertiesFile();
			else
				p = new ProfileImpl();

			if (args.length > 0)
			{
				pps = jade.Boot.parseCmdLineArgs(args);
				
				if (pps != null)
				{
					p = new ProfileImpl(pps);
				}//End IF block
			}//End IF block
			
			/* VERSIONE 3.2 */
			jade.wrapper.AgentContainer mc = null;
			/* VERSIONE 3.1 */
			//jade.wrapper.MainContainer mc = null;

			try
			{
				if (p.getParameter("main", "true").Equals("false"))
					mc = jade.core.Runtime.instance().createAgentContainer(p);
				else
					mc = jade.core.Runtime.instance().createMainContainer(p);
			}
			catch(java.lang.Exception exc)
			{
				System.out.println( "\n----------- INIZIO ECCEZIONE ------------------" );
				System.out.println( exc.getMessage() );
				System.out.println( exc.getLocalizedMessage() );
				System.out.println( "\n----------- FINE ECCEZIONE ------------------" );
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
				System.out.println(pe.getMessage());
				System.out.println("No leap.properties file found!\nUsing default option instead.");
				p = new ProfileImpl();
			}
		}

	}//End AgentBooterJSharp class

