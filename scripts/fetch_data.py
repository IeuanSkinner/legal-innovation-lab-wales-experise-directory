import requests
from bs4 import BeautifulSoup
import json
from cleantext import clean

cos_departments = ['biosciences', 'chemistry', 'compsci', 'geography', 'maths', 'physics']
staff_data = []
missing_aoe = []
missing_page = []
enable_debug = False


def get_staff_data(department):
    url_prefix = 'https://www.swansea.ac.uk/staff/science/'
    url = url_prefix + department
    page = requests.get(url)
    soup = BeautifulSoup(page.content, 'html.parser')

    for row in soup.find('table').find_all('tr'):
        columns = row.find_all('td')

        staff_page_col = columns[0]
        staff_name = clean_text(staff_page_col.text)
        staff_page_link = staff_page_col.find('a')

        # Setup staff page name + url if link exists, taking staff name from link is preferable
        # for Physics department as they sometimes group name + role in this column but not in link.
        if staff_page_link is not None:
            # Some physicists have their link set as name / role hence the need to split here.
            staff_name = clean_text(staff_page_link.text.split('/')[0])
            staff_page_link_url = staff_page_link.get('href')
            # Handle relative URLs
            staff_url = staff_page_link_url if staff_page_link_url.startswith(url_prefix) else url + staff_page_link_url
            expertise = get_expertise(staff_url)

            if len(expertise) > 0:
                # Most departments have 4 column structure, physics is an outlier.
                staff_email_col = columns[1 if department == 'physics' else 2]
                staff_tel_col = columns[2 if department == 'physics' else 3]

                staff_data.append({
                    'name': staff_name,
                    'department': department,
                    'url': staff_url,
                    'email': clean_text(staff_email_col.text),
                    'tel': clean_text(staff_tel_col.text),
                    'expertise': expertise
                })
            else:
                missing_aoe.append(debug_text(staff_name, department))
        else:
            missing_page.append(debug_text(staff_name, department))


def clean_text(text):
    return clean(text, lower=False)


def debug_text(staff_name, department):
    return '[' + staff_name + '] in department [' + department + ']'


def get_expertise(url):
    page = requests.get(url)
    soup = BeautifulSoup(page.content, 'html.parser')
    expertise = []

    expertise_area = soup.find(class_='staff-profile-areas-of-expertise')

    if expertise_area:
        for area in expertise_area.find_all('li'):
            text = clean_text(area.text)
            text = text.replace('* ', '')  # Some staff have an odd bullet point character in their AOE
            text = text.replace(',', '')
            text = text.replace(';', '')

            expertise.append(text)

    return expertise


for dept in cos_departments:
    get_staff_data(dept)

if enable_debug:
    for staff in missing_page:
        print('[MISSING PAGE]: ' + staff)

    for staff in missing_aoe:
        print('[MISSING AOE]: ' + staff)

print('No. of Staff missing pages: {0}'.format(len(missing_page)))
print('No. of Staff missing AOE: {0}'.format(len(missing_aoe)))
print('No. of Staff indexable: {0}'.format(len(staff_data)))

with open('staff_data.json', 'w', encoding='utf-8') as file:
    json.dump(staff_data, file, indent=4)
