___.   .__                   __ ________        
\_ |__ |  | _____    _______/  |\_____  \______ 
 | __ \|  | \__  \  /  ___/\   __\_(__  <_  __ \
 | \_\ \  |__/ __ \_\___ \  |  | /       \  | \/
 |___  /____(____  /____  > |__|/______  /__|   
     \/          \/     \/             \/       
version 0
	(C) 2015 Andrew Breksa [abreksa4@gmail.com]

blast3r uses various open source technologies and data sources, including:
	ttorrent   (http://mpetazzoni.github.io/ttorrent/),
	Strike API (https://getstrike.net/api/)
	and requires nmap for some optional functionality (https://nmap.org/).

blast3r is a tool for finding torrents via json defined "targets" that contain 
a query (or info hash), and optional category and subcategory strings. The gathered
information is saved in json files in the --data-directory. 
When blast3r looks up peers for a torrent, if a json file exists for it
already, those peers are loaded, and added if unique to the new list.

Targets are defined as follows:
In xubuntu14.04.json (under targets/):
    {
    "name" : "xubuntu 14.04",
    "query" : "xubuntu 14.04",
    "hash" : "false",
    "category" : "",
    "subcategory" : ""
    }

To look up all torrents on Strike with that query and fetch peer information from them:
    java -jar blast3r.jar --peers xubuntu14.04

Usage:
	 java -jar blast3r.jar [OPTIONS]

Options: 

 --data-directory (-datad) VAL          : The directory that holds the data
                                          files (default: data/)
 --delete-torrents-on-exit (-dtoe)      : If blast3r should delete the
                                          downloaded torrent files on exit
                                          (default: false)
 --disable-nmap                         : disables using nmap to scan for peers
                                          (default: false)
 --disable-user-agent                   : Disables the user agent header from
                                          being set (default: false)
 --download-directory (-downd) VAL      : The directory that holds the
                                          downloaded files (default: downloads/)
 --hash (-h) VAL                        : Run a search with the specified info
                                          hashes as targets.
 --help (--?, -?)                       : Display this help text and exit
                                          (default: false)
 --info                                 : diaplay extra information in the help
                                          output (default: false)
 --log-file VAL                         : The log file (default: log.log)
 --log-level (-ll) [NONE | ERROR |      : The log level (default: INFO)
 WARN | INFO | DEBUG | TRACE]              
 --log-to-file (-l2f)                   : If blast3r should log to a file
                                          (default: false)
 --loop                                 : If blast3r should loop (update peer
                                          lists) until ctrl+C (default: false)
 --nmap-command-line VAL                : The nmap command line to use to
                                          discover peers (default: nmap
                                          --script bittorrent-discovery
                                          --script-args 'bittorrent-discovery.ma
                                          gnet="%s"' | grep -E -o
                                          "([0-9]{1,3}[\.]){3}[0-9]{1,3}")
 --peers (--get-peers)                  : Get the current peers of each torrent
                                          (default: false)
 --proxy                                : If a SOCKS proxy should be used
                                          (default: false)
 --proxy-ip VAL                         : The SOCKS proxy IP addres (default: )
 --proxy-port N                         : The SOCKS proxy port (default: 0)
 --query (-q) VAL                       : Run a search with the specified query
                                          strings as targets.
 --save-config                          : Saves the provided config to disk
                                          (default: false)
 --strike-api-url VAL                   : The strike api base url (default:
                                          https://getstrike.net/api/v2/)
 --strike-download-url VAL              : The strike download url (default:
                                          https://getstrike.net/torrents/api/dow
                                          nload/%s.torrent)
 --target (-t) VAL                      : The names of the target files to load
 --target-directory (-td) VAL           : The directory which holds the target
                                          files (default: targets/)
 --torrage-url VAL                      : The torrage url (default:
                                          http://torrage.info/download.php?h=%s)
 --torrent-directory VAL                : The directory to save the downloaded
                                          torrent files to (default: torrents/)
 --ttorrent-sleep (-ts) N               : The time in seconds to sleep to wait
                                          for peers (default: 30000)
 --ttorrent-sleep-count (-tsc) N        : The number of times to let ttorrent
                                          sleep to find peers before moving on
                                          to the next method of peer discovery
                                          (default: 3)
 --ttorrent-sleep-peer-count (-tspc) N  : The minimum number of peers for
                                          ttorrent to have before continuing
                                          without using namp. (default: 1)
 --user-agent VAL                       : The user agent to use while accessing
                                          strike and torrage (default:
                                          Mozilla/5.0 (Macintosh; U; Intel Mac
                                          OS X 10.4; en-US; rv:1.9.2.2)
                                          Gecko/20100316 Firefox/3.6.2)
Examples: 

	 java -jar blast3r.jar -q "ubuntu 14.04" --peers --proxy --proxy-ip localhost --proxy-port 1080

Would search for torrent with the query "ubuntu 14.04", and useing the socks proxy provided.
(NOTE: nmap and ttorrent traffic isn't routed through the proxy, so don't use --peers)