import urllib.parse
import requests
import mwparserfromhell


def get_wiki_sources_by_title(*page_titles: str, wiki_host: str = 'wiki.hypixel.net') -> dict[str, mwparserfromhell.wikicode.Wikicode]:
    prepared_titles = "|".join(map(urllib.parse.quote, page_titles))
    api_data = requests.get(f'https://{wiki_host}/api.php?action=query&prop=revisions&titles={prepared_titles}&rvprop=content&format=json&rvslots=main').json()
    if "batchcomplete" not in api_data:
        print(f'Batch data not present in wiki response for: {page_titles}')

    return {
        page['title']: mwparserfromhell.parse(page['revisions'][0]['slots']['main']['*'])
        for _, page in api_data["query"]["pages"].items()
    }
